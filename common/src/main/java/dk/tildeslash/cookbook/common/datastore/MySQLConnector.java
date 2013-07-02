package dk.tildeslash.cookbook.common.datastore;

import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Singleton
 */
public class MySQLConnector implements DataStoreConnector {

    static private MySQLConnector instance = null;

    private String host = null;
    private short port = 0;
    private String database = null;
    private String username = null;
    private String password = null;

    private List<Ingredient> ingredientCache;
    private boolean isIngredientCacheValid = false;

    private static Logger LOGGER = Logger.getLogger(MySQLConnector.class);

    /**
     * Gets the active instance of MySQLConnector. If there is no active instance, one will be created.
     * @return the instance of MySQLConnector.
     * @throws NotConfiguredException if the configuration file used for setting data store parameters are flawed, or not present.
     * @throws DataStoreException if there is a problem connecting to the database.
     */
    static public MySQLConnector getInstance() throws NotConfiguredException, DataStoreException {
        if(instance == null){
            instance = new MySQLConnector();
        }
        return instance;
    }

    /**
     * Sets up the connector and database configuration.
     * @throws NotConfiguredException if Configuration is not loaded, or it is unable to load from the default configuration file.
     * @throws DataStoreException if there is a problem connecting to the database.
     */
    private MySQLConnector() throws NotConfiguredException, DataStoreException {

        //Let us try with the configuration already set
        //or set a new one if we catch an exception
        int tries = 0;
        while(host == null || port == 0 || database == null ||
                username == null || password == null){
            try{
                host = Configuration.getHost();
                port = Configuration.getPort();
                database = Configuration.getDatabase();
                username = Configuration.getDbUser();
                password = Configuration.getDbPassword();
            }
            catch(NotConfiguredException e){
                Configuration.load();
            }
            tries++;
            if(tries == 2){
                break;
            }
        }
        if(tries == 3){
            throw new NotConfiguredException("Unable to load configuration from file");
        }
        //Not sure what to do with these exceptions
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
            LOGGER.error("Exceptions while setting up the connector: " + e.getMessage());
            throw new DataStoreException("Unable to connect to the database");
        }
    }

    @Override //TODO needs testing
    public List<Ingredient> retrieveMostPopularIngredients(int limit, boolean includeCommon) {

        List<Ingredient> ingredients = new LinkedList<>();

        try( Connection conn = getConnection();
             ResultSet rs = executeStatement(conn, "SELECT ingredients.name_singular, ingredients.name_plural, " +
                     "ingredients.common, COUNT(*) AS count FROM ingredients INNER JOIN ingredients_per_section ON " +
                     "ingredients.name_singular=ingredients_per_section.name_singular WHERE common=" + includeCommon + " OR common=0" + //makes sure we always get non common ingredients
                     " GROUP BY ingredients.name_singular ORDER BY count DESC LIMIT " + limit)) {

            if(rs != null){
                while(rs.next()){
                    String singular = rs.getString("name_singular");
                    String plural = rs.getString("name_plural");
                    Boolean common = rs.getBoolean("common");

                    Ingredient ingredient = new Ingredient(singular, plural, common);
                    ingredients.add(ingredient);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return ingredients;
    }

    @Override
    public List<Ingredient> retrieveAllIngredients() throws DataStoreException {
        if ( isIngredientCacheValid ){
            return ingredientCache;
        }

        List<Ingredient> ingredients = new LinkedList<>();

        try (   Connection conn = getConnection();
                ResultSet rs = executeStatement(conn, "SELECT name_singular, name_plural, common FROM ingredients ORDER BY length(name_singular) DESC") ){
            if(rs != null){
                while(rs.next()){
                    String singular = rs.getString("name_singular");
                    String plural = rs.getString("name_plural");
                    Boolean common = rs.getBoolean("common");

                    Ingredient ingredient = new Ingredient(singular, plural, common);
                    ingredients.add(ingredient);
                }
            }
        }
        catch(SQLException e){
            LOGGER.error("Exception while retrieving ingredients (" + e.getMessage() + ")" ) ;
            throw new DataStoreException(e.getMessage());
        }

        ingredientCache = ingredients;
        isIngredientCacheValid = true;
        return ingredients;
    }

    @Override
    public List<Recipe> retrieveAllRecipes() throws DataStoreException {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try (Connection conn = getConnection();
             ResultSet rs = executeStatement(conn, "SELECT name, time_in_minutes, calories, source, portions FROM recipes") ){
            if(rs != null){
                while(rs.next()){
                    Recipe recipe = getRecipeFromResultSet(rs);
                    recipes.add(recipe);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return recipes;
    }

    /**
     * Create a recipe from a result set.
     * @param rs the result set to create the recipe from. Must include the following entries: name, time_in_minutes, calories, source and portions.
     * @return the recipe created from the result set.
     * @throws SQLException if the result set does not contain enough information to create a recipe.
     * @throws DataStoreException if there was a problem retrieving the sections for the recipe.
     */
    private Recipe getRecipeFromResultSet(ResultSet rs) throws SQLException, DataStoreException {
        String name = rs.getString("name");
        int time = rs.getInt("time_in_minutes");
        int calories = rs.getInt("calories");
        String source = rs.getString("source");
        int portions = rs.getInt("portions");

        Recipe newRecipe = new Recipe(name, time, calories, source, new LinkedList<RecipeSection>(), portions);

        return new Recipe(name, time, calories, source, retrieveSections(newRecipe), portions);
    }

    @Override
    public Recipe retrieveRecipeMatchingName(String recipeName) throws DataStoreException {

        try ( Connection conn = getConnection();
              ResultSet rs = executeStatement(conn, "SELECT name, time_in_minutes, calories, source, portions FROM recipes WHERE name='" + escapeQuotes(recipeName) + "'") ){
            if(rs != null){
                rs.next();

                return getRecipeFromResultSet(rs);
            }
        }
        catch(SQLException e){
            //If the result set is empty (i.e. no matching recipe it will throw this exception, it should just be ignored.
        }
        return null;
    }

    @Override
    public List<RecipeSection> retrieveSections(Recipe recipe) throws DataStoreException {
        String recipeName = recipe.getName();
        List<RecipeSection> resultList = null;

        try ( Connection conn1 = getConnection();
              Connection conn2 = getConnection();
              ResultSet sections = executeStatement(conn1, "SELECT id, headline, text " +
                      "FROM sections, sections_per_recipe " +
                      "WHERE recipe_name = '" + escapeQuotes(recipeName) + "' " +
                      "AND sections.id = section_id " +
                      "ORDER BY placement ASC") ){

            if(sections != null){
                resultList = new ArrayList<>();
                //Loop through all the sections and create them
                while(sections.next()){
                    int id = sections.getInt("id");
                    String headline =sections.getString("headline");
                    String text = sections.getString("text");
                    List<RecipeIngredient> ingredientList = new LinkedList<>();

                    String statement = "SELECT ingredients.name_singular AS name_singular, " +
                            "name_plural, common, suffix, prefix " +
                            "FROM ingredients, ingredients_per_section " +
                            "WHERE section_id = " + id +
                            " AND ingredients.name_singular = ingredients_per_section.name_singular";

                    ResultSet ingredients = executeStatement(conn2, statement);

                    if(ingredients != null){
                        while(ingredients.next()){
                            //Setup Ingredient
                            String nameSingular = ingredients.getString("name_singular");
                            String namePlural = ingredients.getString("name_plural");
                            boolean common = ingredients.getBoolean("common");
                            Ingredient ingredient = new Ingredient(nameSingular, namePlural, common);

                            //Setup RecipeIngredient
                            String suffix = ingredients.getString("suffix");
                            String prefix = ingredients.getString("prefix");
                            try{
                                RecipeIngredient recipeIngredient = new RecipeIngredient(prefix, ingredient, suffix);
                                ingredientList.add(recipeIngredient);
                            } catch (IllegalArgumentException e){
                                throw new DataStoreException("Could not create RecipeIngredient from the data retrieved from the store." +
                                        "ingredient: " + ingredient +
                                        ", prefix" + prefix +
                                        ", suffix: " + suffix);
                            }
                        }
                    }
                    resultList.add(new RecipeSection(headline, text, ingredientList));
                }
            }
        }
        catch(SQLException e){
            LOGGER.error("Exception while retrieving sections for recipe (" + recipe.getName() +"): " + e.getMessage());
        }

        return resultList;
    }

    @Override
    public boolean removeIngredient(Ingredient ingredient){
        isIngredientCacheValid = false;

        try{
            executeUpdate("DELETE FROM ingredients WHERE name_singular = '" + escapeQuotes(ingredient.getSingular()) + "'");
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean removeRecipe(Recipe recipe){
        String statement = createRemoveRecipeStatement(recipe);
        try{
            executeUpdate(statement);
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates a sql statement that will remove a recipe, all its sections and ingredient reference.
     * @param recipe the recipe to create remove statement for.
     * @return a string containing the sql statement that will remove the recipe.
     */
    private String createRemoveRecipeStatement(Recipe recipe){
        String removeStatement = "DELETE sections.*, recipes.* from sections_per_recipe INNER JOIN " +
                "(recipes, sections) ON (recipes.name=sections_per_recipe.recipe_name " +
                "AND sections.id=sections_per_recipe.section_id) " +
                "WHERE recipe_name='" + escapeQuotes(recipe.getName() + "\'");

        if(removeStatement.endsWith("''")){
            removeStatement = removeStatement.substring(0, removeStatement.length()-1);
        }

        return removeStatement;
    }

    @Override
    public boolean addIngredient(Ingredient ingredient){
        isIngredientCacheValid = false;

        try{
            String statement = "REPLACE INTO ingredients (name_singular, name_plural, common) VALUES ('" + escapeQuotes(ingredient.getSingular()) + "', '" +
                    escapeQuotes(ingredient.getPlural()) + "', " +
                    ingredient.isCommon() + ")";
            executeUpdate(statement);
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean addRecipe(Recipe recipe){
        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                port + "/" + database + "?user=" +
                username + "&password=" + password +
                "&default-character-set=utf8") ){
            Statement stmt = conn.createStatement();

            createAddRecipeBatch(stmt, recipe);
            stmt.executeBatch();
        }
        catch(SQLException | DataStoreException e){
            LOGGER.error("Exception while adding recipe: " + e.getMessage());
            return false;
        }
        return true;

    }

    /**
     * adds a sql batch to the statement that will create the recipe in the data store.
     * @param stmt the statement to add the sql batch too.
     * @param recipe the recipe to be added tot he datastore.
     * @throws SQLException if there is a problem adding the batch to the statement.
     * @throws DataStoreException if there is a problem retrieving the ingredients needed to calculate the sql batch.
     */
    private void createAddRecipeBatch(Statement stmt, Recipe recipe) throws SQLException, DataStoreException{
        List<RecipeSection> sections = recipe.getSections();
        List<Ingredient> ingredients = retrieveAllIngredients();

        stmt.addBatch("INSERT INTO recipes VALUES ('" + escapeQuotes(recipe.getName()) + "', " +
                recipe.getTime() + ", " + recipe.getImage() + ", " + recipe.getCalories() + ", '" + recipe.getSource() + "', " + recipe.getPortions() + ")");

        int count = 0;
        //Add sections
        for(RecipeSection section: sections){
            //Add all unknown ingredients.
            for(RecipeIngredient recipeIngredient: section.getIngredients()){
                Ingredient ingredient = recipeIngredient.getIngredient();
                if(!ingredients.contains(ingredient)){
                    stmt.addBatch("INSERT INTO ingredients (name_singular, name_plural, common) VALUES ('" + escapeQuotes(ingredient.getSingular()) + "', '" +
                            escapeQuotes(ingredient.getPlural()) + "', " +
                            ingredient.isCommon() + ")");
                    ingredients.add(ingredient);
                    isIngredientCacheValid = false;
                }
            }

            //Add SectionIngredients and link
            stmt.addBatch("INSERT INTO sections (headline, text) VALUES ('" + escapeQuotes(section.getHeadline()) +
                    "', '" + escapeQuotes(section.getText()) + "')");
            stmt.addBatch("SET @LAST_ID = LAST_INSERT_ID()");
            for(RecipeIngredient recipeIngredient: section.getIngredients()){
                Ingredient ingredient = recipeIngredient.getIngredient();
                stmt.addBatch("INSERT INTO ingredients_per_section (section_id, name_singular, suffix, prefix) SELECT @LAST_ID, '" + escapeQuotes(ingredient.getSingular()) +
                        "', '" +  escapeQuotes(recipeIngredient.getSuffix()) + "', '" +
                        escapeQuotes(recipeIngredient.getPrefix()) + "'");
            }

            count++;

            stmt.addBatch("INSERT INTO sections_per_recipe () SELECT '" + escapeQuotes(recipe.getName()) +
                    "', @LAST_ID, " + count);
        }
    }

    @Override
    public boolean updateIngredient(Ingredient oldIngredient, Ingredient newIngredient){
        isIngredientCacheValid = false;

        try{
            String statement = "UPDATE ingredients SET name_singular='" +
                    escapeQuotes(newIngredient.getSingular().toLowerCase()) + "', name_plural='" +
                    escapeQuotes(newIngredient.getPlural().toLowerCase()) + "', common=" +
                    newIngredient.isCommon() + " WHERE name_singular='" +
                    escapeQuotes(oldIngredient.getSingular().toLowerCase()) + "'";
            executeUpdate(statement);
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /***
     * Executes an update on the data base.
     * @param statement the statement to execute.
     * @return the result from a call to java.sql.Statement.executeUpdate(statement);
     * @throws SQLException if there is a problem setting up the connection, or in executing the update.
     */
    private int executeUpdate(String statement) throws SQLException{
        isIngredientCacheValid = false;

        //Setup connection
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                port + "/" + database + "?user=" +
                username + "&password=" + password +
                "&default-character-set=utf8")) {
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(statement);
        } catch (SQLException e) {
            LOGGER.error("Exception while executing update: " + e.getMessage());
            throw e;
        }
    }

    private Connection getConnection(){
        try{
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                    port + "/" + database + "?user=" +
                    username + "&password=" + password +
                    "&default-character-set=utf8");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Execute a statement on the data base.
     * @param conn the connection to use while executing the statement. The connection needs to be supplied to make sure it is not closed before the result set is read.
     * @param statement the statement to execute.
     * @return null if the statement did not execute, otherwise the result set resulting from executing the statement.
     * @throws SQLException if there is a problem connecting to the database, or executing the statement.
     */
    private ResultSet executeStatement(Connection conn, String statement) throws SQLException{
        isIngredientCacheValid = false;

        ResultSet rs = null;

        Statement stmt = conn.createStatement();

        if(stmt.execute(statement)){
            rs = stmt.getResultSet();
        }
        return rs;
    }

    /**
     * Escapes any quotes in the input to prevent sql injections.
     * @param input string to sanitize.
     * @return the input string with quotes escaped.
     */
    private String escapeQuotes(String input){
        return input.replaceAll("'", "''");
    }

    @Override
    public Ingredient retrieveIngredient(String name) throws DataStoreException{

        name = name.toLowerCase();

        if( isIngredientCacheValid ){
            for ( Ingredient ingredient : ingredientCache ) {
                if( ingredient.getSingular().equals("name")){
                    return ingredient;
                }
            }
        }

        Ingredient result = null;
        try ( Connection conn = getConnection();
              ResultSet rs = executeStatement(conn,
                      "SELECT name_singular, name_plural, common FROM ingredients WHERE " +
                              "name_singular='" + escapeQuotes(name) + "' OR " +
                              "name_plural='" + escapeQuotes(name) + "'")){

            if(rs != null){
                while(rs.next()){
                    String singular = rs.getString("name_singular");
                    String plural = rs.getString("name_plural");
                    Boolean common = rs.getBoolean("common");
                    result = new Ingredient(singular, plural, common);
                }
            }

        } catch (SQLException e){
            LOGGER.error("Exception while retrieving ingredient: " + e.getMessage());
            throw new DataStoreException(e.getMessage());
        }
        return result;
    }


    /**
     * Invalidates any caches that the connector might hold.
     * Should not be used outside unit testing as it is not
     * part of the DataStoreConnector interface.
     */
    void invalidateCaches(){
        isIngredientCacheValid = false;
    }

    @Override
    public boolean updateRecipe(Recipe oldRecipe, Recipe newRecipe){

        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                port + "/" + database + "?user=" +
                username + "&password=" + password +
                "&default-character-set=utf8");
              Statement stmt = conn.createStatement() ) {

            String removeStatement = createRemoveRecipeStatement(oldRecipe);

            stmt.addBatch(removeStatement);
            createAddRecipeBatch(stmt, newRecipe);

            stmt.executeBatch();

        }
        catch(SQLException | DataStoreException e){
            LOGGER.error("Exception while updating recipe (" + oldRecipe.getName() + "), " + e.getMessage());
            return false;
        }

        return true;
    }
}