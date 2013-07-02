package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;


public interface AddIngredientListener {

    public void addIngredientEvent(RecipeIngredient ingredient);
}
