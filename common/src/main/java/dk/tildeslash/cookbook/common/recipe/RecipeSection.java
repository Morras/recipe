package dk.tildeslash.cookbook.common.recipe;

import java.util.List;

public class RecipeSection{

	private String headline = "";
	private String text = "";
	private List<RecipeIngredient> ingredients;
	private int cachedHash = 0;

	public RecipeSection(String headline, String text, List<RecipeIngredient> ingredients){
		if(headline == null){
			this.headline = "";
		} else {
			this.headline = headline;
		}
		if(text == null){
			text = "";
		}
	    this.text = text;

		this.ingredients = ingredients;
	}

	public String getHeadline(){
		return headline;
	}

	public String getText(){
		return text;
	}

	public List<RecipeIngredient> getIngredients(){
		return ingredients;
	}

	public boolean equals(Object o){
		if(o != null && o.getClass() == o.getClass()){
			RecipeSection other = (RecipeSection)o;
			return headline.equals(other.getHeadline()) &&
				text.equals(other.getText()) &&
				ingredients.equals(other.getIngredients());
		} else {
			return false;
		}
	}

	public int hashCode(){
		if(cachedHash == 0){
			String hashString = headline + text;
			cachedHash = hashString.hashCode() ^ ingredients.hashCode();
		}
		return cachedHash;
	}
}
