package dk.tildeslash.cookbook.common.recipe;

import java.util.LinkedList;
import java.util.List;

public class Recipe implements Comparable{
	private int time;
    private String name;
    //TODO: Image
    private String source;
    private int calories;
    private List<RecipeSection> sections;
    private int cachedHash = 0;
    private int portions = 1;

	/**
	 * @param name the name of the recipe.
	 * @param time the time it takes to make the food in minutes.
	 * @param source the source of the recipe.
	 * @param calories the amount of calories in the recipe.
	 * @param sections a linked list of RecipeSections in order for the recipe.
     * @param portions the number of persons the recipe is for, alternatively the number of pieces the recipe produces.
	 */
	public Recipe(String name, int time, int calories, String source, List<RecipeSection> sections, int portions){

		if(name == null){
		    this.name = "";
		} else {
			this.name = name;
		}
		this.time = time;
		this.calories = calories;
		if(source == null){
			this.source = "";
		} else {
		this.source = source;
		}
		if(sections == null){
			this.sections = new LinkedList<>();
		} else {
		this.sections = sections;
		}

        if ( portions <= 0 ){
            throw new IllegalArgumentException("The recipe has to have a positive portions size");
        }
        this.portions = portions;
	}

	public int getTime(){
		return time;
	}

	public int getCalories(){
		return calories;
	}

	public String getName(){
		return name;
	}

	public String getSource(){
		return source;
	}
	
	public List<RecipeSection> getSections(){
		return sections;
	}
	
	public Object getImage(){
		return null;
	}

    public int getPortions(){
        return portions;
    }

	@Override
		public boolean equals(Object o){
			if(o != null && o.getClass() == this.getClass()){
				Recipe r = (Recipe)o;
				return name.equals(r.getName());
			} else {
				return false;
			}
		}

	@Override
		public int hashCode(){
			if(cachedHash == 0){
				cachedHash = name.hashCode();
			}
			return cachedHash;
		}
	
	@Override
		public String toString(){
				return name;
		}

    @Override
    public int compareTo(Object o) {
        Recipe other = (Recipe)o;
        return name.compareTo(other.getName());
    }
}