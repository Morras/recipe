package dk.tildeslash.cookbook.common.recipe;

/**
 * An ingredient contains information about its name
 * in singular and plural form, as well as an indication
 * of whether it is an ingredient that is common in
 * the kitchen.
 */
public class Ingredient implements Comparable{

	private String namePlural; //always stored in lower case
	private String nameSingular; //always stored in lower case
	private boolean common;
	private int cachedHashCode = 0;

	public Ingredient(String nameSingular, String namePlural, boolean common){
		this.namePlural = namePlural.toLowerCase();
		this.nameSingular = nameSingular.toLowerCase();
		this.common = common;
	}

    /**
     * Gets the plural name of the ingredient.
     * @return plural name of the ingredient.
     */
	public String getPlural(){
		return namePlural;
	}

    /**
     * Gets the singular name of the ingredient.
     * @return singular name of the ingredient.
     */
	public String getSingular(){
		return nameSingular;
	}

    /**
     * Indicated if the ingredient is expected
     * in a common kitchen.
     * @return true if the ingredient is usually found in the kitchen.
     */
	public boolean isCommon(){
		return common;
	}

	@Override
	public boolean equals(Object object){
		if(object != null && object.getClass() == this.getClass()){
			Ingredient ingredient = (Ingredient)object;
			return nameSingular.equals(ingredient.getSingular());
		} else {
			return false;
		}
	}

	@Override
		public int hashCode(){
		if(cachedHashCode == 0){
			cachedHashCode = nameSingular.hashCode();
		}		
		return cachedHashCode;
		}

    @Override
        public String toString(){
            return nameSingular;
        }

    @Override
    public int compareTo(Object o) {
        Ingredient other = (Ingredient)o;
        return nameSingular.compareTo(other.getSingular());
    }
}