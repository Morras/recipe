package dk.tildeslash.cookbook.common.recipe;

import org.junit.*;
import com.gargoylesoftware.base.testing.EqualsTester;

import static junit.framework.Assert.assertEquals;

public class IngredientTest {

	//Constructor and sets are to simple to test
	//only need to test equals
	private Ingredient ingredient = new Ingredient("Foo", "Foos", true);
	private Ingredient ingredientSame = new Ingredient("Foo", "Foos", true);
	private Ingredient ingredientDifferent = new Ingredient("bar", "Foos", true);
	private Ingredient ingredientSubclass = new Ingredient("Foo", "Foos", true) {};

	@Test
		public void EqualIngredientDifferentSingularTest(){
			new EqualsTester(ingredient, ingredientSame, ingredientDifferent, ingredientSubclass);
		}

    @Test
        public void toStringTest(){
            assertEquals("toString should return the singular name", "foo", ingredient.toString());

    }
}
