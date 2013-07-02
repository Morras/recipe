package dk.tildeslash.cookbook.common.recipe;

import org.junit.*;
import com.gargoylesoftware.base.testing.EqualsTester;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/*
 * Recipe does only declare get
 * and set methods that are to 
 * simple to test, so so far we
 * only test equals.
 */


public class RecipeTest {

	private Recipe recipe = new Recipe("Name", 10, 10, "Source", null, 2);
	private Recipe recipeSame = new Recipe("Name", 10, 10, "Source", null, 2);
	private Recipe recipeDifferentName = new Recipe("Other name", 10, 10, "Source", null, 2);
	private Recipe recipeSubclass = new Recipe("Name", 10, 10, "Source", null, 2){};

	@Test
		public void EqualRecipeDifferentNameTest(){
			new EqualsTester(recipe, recipeSame, recipeDifferentName, recipeSubclass);
		}

    @Test
    public void NullNameConvertsToEmpty(){
        Recipe recipe = new Recipe(null, 2, 2, "", new ArrayList<RecipeSection>(), 2);
        assertEquals("An empty name in the constructor should be converted to \"\" to prevent null pointers.", "", recipe.getName());
    }

    @Test
    public void toStringReturnsName(){
        assertEquals("toString method should return the recipe name.", "Other name", recipeDifferentName.toString());
    }
}
