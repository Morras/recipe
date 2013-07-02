package dk.tildeslash.cookbook.common.datastore;

import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;

import java.util.List;

public interface DataStoreConnector {

    /**
     * Retrieves the ingredients that has been used in most times.
     * If a recipe uses the same ingredient multiple times, this may
     * be counted as multiple uses of the ingredient.
     * @param limit the number of ingredients that should be returned.
     * @param includeCommon if true, the result will include ingredients marked as common.
     * @return A list of the most popular ingredients based on usage counting.
     * @throws DataStoreException if there is a problem retrieving the ingredients from the database.
     */
    List<Ingredient> retrieveMostPopularIngredients(int limit, boolean includeCommon) throws DataStoreException;

    /**
     * Retrieves all ingredients from the database
     * @return a list of all ingredients in the database.
     * @throws DataStoreException if there is a problem retrieving the ingredients from the data store.
     */
	List<Ingredient> retrieveAllIngredients() throws DataStoreException;

    /**
     * Retrieves all recipes from the database
     * @return a list of all recipes in the database.
     * @throws DataStoreException if there is a problem retrieving the recipes from the datas tore.
     */
	List<Recipe> retrieveAllRecipes() throws DataStoreException;

    /**
     * Retrieves a list containing the sections for a given recipe
     * @param recipe the recipe for which to retrieve the sections.
     * @return a list of sections that is included in the recipe.
     * @throws DataStoreException if there is a problem connecting to the data store, or retrieving the sections.
     */
	List<RecipeSection> retrieveSections(Recipe recipe) throws DataStoreException;

    /**
     * Retrieves a recipe matching a given name.
     * @param name the name of the recipe to retrieve.
     * @return the recipe matching the name.
     * @throws DataStoreException if there is a problem connecting to the data store, or retrieving the recipe.
     */
	Recipe retrieveRecipeMatchingName(String name) throws DataStoreException;

    /**
     * Adds a recipe to the data store
     * @param recipe the recipe to add.
     * @return true if the recipe was successfully added, false otherwise.
     */
	boolean addRecipe(Recipe recipe);

    /**
     * Adds an ingredient to the data store
     * @param ingredient the ingredient to add.
     * @return true if the ingredient was successfully added, false otherwise.
     */
	boolean addIngredient(Ingredient ingredient);

    /**
     * Removes an ingredient from the data store.
     * @param ingredient the ingredient to be removed
     * @return true if the ingredient was successfully removed, false otherwise.
     */
	boolean removeIngredient(Ingredient ingredient);

    /**
     * Removes a recipe from the data store.
     * @param recipe the recipe to be removed
     * @return true if the recipe was successfully removed, false otherwise.
     */
	boolean removeRecipe(Recipe recipe);

    /**
     * Updates an ingredient in the data store.
     * @param oldIngredient the ingredient to be updated.
     * @param newIngredient the ingredient that should replace the old ingredient.
     * @return true if the ingredient was successfully updated, false otherwise.
     */
	boolean updateIngredient(Ingredient oldIngredient, Ingredient newIngredient);

    /***
     * Retrieves an ingredient from the data store, by searching for the singular name of the recipe.
     * @param singular the singular name of the ingredient to retrieve.
     * @return the ingredient matching the name supplied.
     * @throws DataStoreException if there is a problem connecting to the data store, or retrieving the ingredient.
     */
    Ingredient retrieveIngredient(String singular) throws DataStoreException;

    /**
     * Updates a recipe in the data store.
     * @param oldRecipe the recipe to be updated.
     * @param newRecipe the recipe that should replace the old ingredient.
     * @return true if the recipe was successfully updated, false otherwise.
     */
    boolean updateRecipe(Recipe oldRecipe, Recipe newRecipe);
}
