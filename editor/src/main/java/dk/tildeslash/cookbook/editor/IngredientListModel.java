package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class IngredientListModel extends AbstractListModel<RecipeIngredient> {

	private List<RecipeIngredient> ingredients = new ArrayList<>();

	@Override
	public RecipeIngredient getElementAt(int index) {
		return ingredients.get(index);
	}

	@Override
	public int getSize() {
		return ingredients.size();
	}

	public void add(RecipeIngredient i){
		ingredients.add(i);
	}

    public void add(int index, RecipeIngredient i){
        ingredients.add(index, i);
    }

	public void remove(int i){
		ingredients.remove(i);
	}

    public void remove(RecipeIngredient ingredient){
        ingredients.remove(ingredient);
    }

    public List<RecipeIngredient> getIngredients(){
        return ingredients;
    }
}
