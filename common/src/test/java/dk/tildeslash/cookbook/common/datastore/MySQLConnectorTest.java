package dk.tildeslash.cookbook.common.datastore;

import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static junit.framework.Assert.*;

public class MySQLConnectorTest {

    //Represent a real section from the test database
    private Ingredient ingredient1 = new Ingredient("porre", "porrer", false);
    private Ingredient ingredient2 = new Ingredient("gulerod", "gulerødder", false);
    private Ingredient ingredient3 = new Ingredient("inderfilet af kyllingebryst", "inderfilet af kyllingebryst", false);

    private RecipeIngredient recipeIngredient1 = new RecipeIngredient("4", ingredient1, "");
    private RecipeIngredient recipeIngredient2 = new RecipeIngredient("6", ingredient2, null);
    private RecipeIngredient recipeIngredient3 = new RecipeIngredient("550 gram", ingredient3, "");

    private List<RecipeIngredient> ingredients = Arrays.asList(recipeIngredient1, recipeIngredient2, recipeIngredient3);

    private RecipeSection realSection = new RecipeSection(null, "Skær roden af porrerne og fjern de yderste, grove blade. Skræl gulerødderne. Skær grønsagerne i tynde strimler og stave. Skær grove sener og hinder af inderfileterne og skær kødet i ca. 2 cm stykker.", ingredients);

    private static DataStoreConnector helper;
    private Recipe recipe2 = new Recipe("Ragout med kylling og grønsager",
            30,
            100,
            "http://arla.dk/opskrifter/Ragout-med-kylling-og-gronsager/",
            new LinkedList<RecipeSection>(),
            2);

    private Recipe recipe1 = new Recipe("Frisk pasta med tun og citron",
            0,
            0,
            "http://www.arla.dk/da/opskrifter/frisk-pasta-med-tun-og-citron/",
            new LinkedList<RecipeSection>(),
            2);

    private Ingredient vand = new Ingredient("vand", "vand", true);
    private Ingredient citronskal = new Ingredient("citronskal", "citronskal", false);

    private Ingredient dummyIngredient = new Ingredient("Bar", "baz", true);
    private RecipeIngredient dummyRecipeIngredient = new RecipeIngredient("", dummyIngredient, "");
    private List<RecipeIngredient> ingredients2 = Arrays.asList(dummyRecipeIngredient, recipeIngredient2, recipeIngredient3);
    private RecipeSection dummySection = new RecipeSection(null, "Foo section", ingredients2);

    private Recipe dummyRecipe = new Recipe("Foo recipe", 0, 0, null, Arrays.asList(dummySection, realSection), 2);

    //An array of strings to be executed to reset the database.
    private static String[] sqlResetBatch;
    private static String[] sqlInsertBatch;

    private static String host = null;
    private static short port = 0;
    private static String database = null;
    private static String username = null;
    private static String password = null;

    private static void loadDBConfiguration() throws NotConfiguredException {
        host = Configuration.getHost();
        port = Configuration.getPort();
        database = Configuration.getDatabase();
        username = Configuration.getDbUser();
        password = Configuration.getDbPassword();
    }

    /**
     * Need to be called manually for each test that
     * modifies the database. This could be done manually
     * but that takes more resources.
     */
    private static void resetDatabase() throws DataStoreException{

        try{
            if ( ! Configuration.isConfigured() ){
                URL testFile = MySQLConnectorTest.class.getResource("test.conf");
                Configuration.load(new File(testFile.toURI()));
            }
            loadDBConfiguration();
        } catch (NotConfiguredException | URISyntaxException | IOException e) {
            //If we get here we are unable to load the configuration file, so test setup is wrong.
            throw new RuntimeException(e);
        }

        try(
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                    port + "/" + database + "?user=" +
                    username + "&password=" + password +
                    "&default-character-set=utf8") ){

            Statement resetStatement = conn.createStatement();
            for(String statement: sqlResetBatch){
                resetStatement.addBatch(statement);
            }
            for(String statement: sqlInsertBatch){
                resetStatement.addBatch(statement);
            }
            resetStatement.executeBatch();

            MySQLConnector.getInstance().invalidateCaches();
        } catch (SQLException | NotConfiguredException e){
            //If we get here we are unable to load the configuration file, so test setup is wrong.
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setUp() throws IOException, NotConfiguredException, DataStoreException{
        InputStream sqlInsertsFile = MySQLConnectorTest.class.getResourceAsStream("sql_inserts.sql");
        InputStream sqlResetTablesFile = MySQLConnectorTest.class.getResourceAsStream("sql_reset_tables.sql");

        StringBuilder batch = createBatch(sqlResetTablesFile);

        sqlResetBatch = batch.toString().split(";");

        batch = createBatch(sqlInsertsFile);

        sqlInsertBatch = batch.toString().split(";");

        resetDatabase();

        helper = MySQLConnector.getInstance();
    }

    private static StringBuilder createBatch(InputStream sqlFile) throws IOException{

        StringBuilder batch = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(sqlFile)) ){

            String line = reader.readLine();
            while(line != null){
                //Ignore lines starting with #
                if(!line.startsWith("#")){
                    batch.append(line);
                }
                line = reader.readLine();
            }
            return batch;
        }
    }

    @Test
    public void RetrieveRecipesReturn2Recipes() throws DataStoreException {
        List<Recipe> recipes = helper.retrieveAllRecipes();
        assertEquals("There should be 2 entries in the test database", 2, recipes.size());
    }



    @Test
    public void RetrieveRecipeContainsRecipe1() throws DataStoreException {
        List<Recipe> recipes = new LinkedList<>();
        assertFalse("The list is empty and should not contain the recipe 'Frisk pasta med tun og citron'", recipes.contains(recipe1));
        recipes = helper.retrieveAllRecipes();
        //Contains 'frisk pasta med tun og citron'
        assertTrue("There should be a recipe called 'Frisk pasta med tun og citron'",
                recipes.contains(recipe1));
    }

    @Test
    public void RetrieveRecipeContainsRecipe2() throws DataStoreException {
        List<Recipe> recipes = new LinkedList<>();
        assertFalse("The list is empty and should not contain the recipe 'Ragout med kylling og grønsager", recipes.contains(recipe2));
        recipes = helper.retrieveAllRecipes();
        //Contains 'Ragout med kylling og grønsager'
        assertTrue("There should be a recipe called 'Ragout med kylling og grønsager'",
                recipes.contains(recipe2));
    }

    @Test
    public void RetrieveIngredientReturn20() throws DataStoreException{
        List<Ingredient> ingredients = new LinkedList<>();
        assertTrue("The list should be empty", ingredients.isEmpty());
        ingredients = helper.retrieveAllIngredients();
        assertEquals("The list should contain 20 ingredients.", 20, ingredients.size());
    }

    @Test
    public void RetrieveIngredientsContainVand() throws DataStoreException{
        List<Ingredient> ingredients = new LinkedList<>();
        assertFalse("The list is empty and should not contain the ingredient 'vand'", ingredients.contains(vand));
        ingredients = helper.retrieveAllIngredients();
        assertTrue("There should be an ingredients called 'vand'",
                ingredients.contains(vand));
    }

    @Test
    public void RetrieveIngredientsContainCitronskal() throws DataStoreException{
        List<Ingredient> ingredients = new LinkedList<>();
        assertFalse("The list is empty and should not contain the ingredient 'citronskal'", ingredients.contains(vand));
        ingredients = helper.retrieveAllIngredients();
        assertTrue("There should be an ingredients called 'citronskal'",
                ingredients.contains(citronskal));
    }

    @Test
    public void RetrieveRecipeMatchingNameTest() throws DataStoreException {
        assertEquals(recipe1, helper.retrieveRecipeMatchingName("Frisk pasta med tun og citron"));
    }

    @Test
    public void RetrieveSectionsTest() throws DataStoreException {
        List<RecipeSection> sections = new LinkedList<>();
        assertFalse("The list is empty and should not contain the recipe section.", sections.contains(realSection));
        sections = helper.retrieveSections(recipe2);
        assertTrue("The helper should have retrieved the real section from the database, and put it in the returning list.",
                sections.contains(realSection));
    }

    @Test
    public void addIngredientTest() throws DataStoreException {
        List<Ingredient> ingredients = helper.retrieveAllIngredients();
        int oldSize = ingredients.size();
        Ingredient ingredient = new Ingredient("kommen", "kommen", false);
        assertTrue("addIngredient should succeed and return true", helper.addIngredient(ingredient));
        assertFalse("The new ingredient is not already in the list", ingredients.contains(ingredient));
        ingredients = helper.retrieveAllIngredients();
        assertEquals("Size should increase when new ingredient is added", oldSize+1, ingredients.size());
        assertTrue("the new ingredient should now be in the list", ingredients.contains(ingredient));
        resetDatabase();
    }

    @Test
    public void removeIngredientTest() throws SQLException, DataStoreException {
        List<Ingredient> ingredients = helper.retrieveAllIngredients();
        int oldSize = ingredients.size();
        assertTrue("The ingredients contains porrer.", ingredients.contains(ingredient1));
        assertTrue("removeIngredient should succeed and return true", helper.removeIngredient(ingredient1));
        ingredients = helper.retrieveAllIngredients();
        assertEquals("The list has decreased with 1", oldSize-1, ingredients.size());
        assertFalse("The ingredients no longer contains porrer.", ingredients.contains(ingredient1));
        resetDatabase();
    }

    @Test
    public void removeRecipeTest() throws SQLException, DataStoreException {
        assertNotNull("The recipe should exist.", helper.retrieveRecipeMatchingName("Frisk pasta med tun og citron"));
        assertTrue("removeRecipe should succeed and return true", helper.removeRecipe(recipe1));
        assertNull("The recipe should no longer exist.", helper.retrieveRecipeMatchingName("Frisk pasta med tun og citron"));
        resetDatabase();
    }

    @Test
    public void updateIngredientTest() throws DataStoreException {
        assertTrue("The old ingredient has not yet been removed.", helper.retrieveAllIngredients().contains(ingredient1));
        Ingredient newIngredient = new Ingredient("foo", "bar", true);
        assertTrue("updateIngredient should succeed and return true", helper.updateIngredient(ingredient1, newIngredient));
        List<Ingredient> ingredients = helper.retrieveAllIngredients();
        assertFalse("The old ingredient is no longer in the database.", ingredients.contains(ingredient1));
        assertTrue("The new ingredient is now in the database.", ingredients.contains(newIngredient));
        resetDatabase();
    }

    @Test
    public void addRecipeTest() throws DataStoreException {
        assertFalse("The recipe does not exists before it is added.", helper.retrieveAllRecipes().contains(dummyRecipe));
        assertTrue("addRecipe should succeed and return true", helper.addRecipe(dummyRecipe));
        assertTrue("The recipe does exists after it is added.", helper.retrieveAllRecipes().contains(dummyRecipe));
        resetDatabase();
    }

    @Test
    public void addRecipeAddsSections() throws DataStoreException {
        assertTrue("addRecipe should succeed and return true", helper.addRecipe(dummyRecipe));
        List<RecipeSection> sections = helper.retrieveSections(dummyRecipe);
        assertTrue("The recipe should have the dummy section", sections.contains(dummySection));
        assertTrue("The recipe should have the real section", sections.contains(realSection));
        resetDatabase();
    }

    @Test
    public void addRecipeAddsNewIngredients() throws DataStoreException {
        List<Ingredient> oldIngredients = helper.retrieveAllIngredients();
        int oldIngredientsSize = oldIngredients.size();

        assertFalse("The ingredients should not contain the dummy ingredient before being added.",
                oldIngredients.contains(dummyIngredient));

        assertTrue("addRecipe should succeed and return true", helper.addRecipe(dummyRecipe));

        List<Ingredient> newIngredients = helper.retrieveAllIngredients();
        assertTrue("The ingredients should contain the dummy ingredient after recipe has been added.",
                newIngredients.contains(dummyIngredient));

        assertEquals("Adding a recipe with 1 new ingredient should only add 1 new ingredient to the ingredients list.", oldIngredientsSize+1, newIngredients.size());
        resetDatabase();
    }

    @Test
    public void updateRecipeMaintainsNumberOfRecipes() throws DataStoreException {
        List<Recipe> oldRecipes = helper.retrieveAllRecipes();
        helper.updateRecipe(recipe1, dummyRecipe);
        List<Recipe> newRecipes = helper.retrieveAllRecipes();
        assertEquals("Updating a recipe should not change the number of total recipes in the database.", oldRecipes.size(), newRecipes.size());
        resetDatabase();
    }

    @Test
    public void updateRecipeRemovesOldRecipe() throws DataStoreException {
        List<Recipe> oldRecipes = helper.retrieveAllRecipes();
        assertTrue("Old recipe should exists before we update it.", oldRecipes.contains(recipe1));
        helper.updateRecipe(recipe1, dummyRecipe);
        List<Recipe> newRecipes = helper.retrieveAllRecipes();
        assertFalse("Old recipe should not exists after it is updated.", newRecipes.contains(recipe1));
        resetDatabase();
    }

    @Test
    public void updateRecipeAddsNewRecipe() throws DataStoreException {
        List<Recipe> oldRecipes = helper.retrieveAllRecipes();
        assertFalse("New recipe should not exists before we update the old one.", oldRecipes.contains(dummyRecipe));
        helper.updateRecipe(recipe1, dummyRecipe);
        List<Recipe> newRecipes = helper.retrieveAllRecipes();
        assertTrue("New recipe should exists after the old one is updated.", newRecipes.contains(dummyRecipe));
        resetDatabase();
    }
}