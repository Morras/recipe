package dk.tildeslash.cookbook.common.recipe;

import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RecipeIngredientFactoryTest {

    /**
     * The following tests are legacy tests from when the RecipeIngredient was constructed with amount, unit, prefix and suffix
     * The data model has since been changed, but the tests remain since they ensure correctness in some corner cases
     * that still apply to the new data model
     */


    @Test
    public void onlyIngredient() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("", new Ingredient("salt", "salt", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndSuffix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("", new Ingredient("salt", "salt", false), "foo");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("salt foo");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndPrefix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("foo", new Ingredient("salt", "salt", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("foo salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndAmountWithComma() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("1,5", new Ingredient("salt", "salt", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("1,5 salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndAmountWithPeriod() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("1.5", new Ingredient("salt", "salt", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("1.5 salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndUnitAndAmount() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("1.5 gram", new Ingredient("salt", "salt", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("1.5 gram salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientGreedyName() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("", new Ingredient("groft salt", "groft salt", true), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("groft salt");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void ingredientAndAllParameters() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("1.5 gram foo", new Ingredient("salt", "salt", false), "bar");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("1.5 gram foo salt bar");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void pluralIngredient() throws NotConfiguredException, DataStoreException {
        RecipeIngredient reference = new RecipeIngredient("2",new Ingredient("gulerod", "gulerødder", false), "");
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("2 gulerødder");
        assertEquals("Reference and test should equal each other", reference, test);
    }

    @Test
    public void noExsistingIngredient() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("2 foo");
        assertNull("If the ingredient does not exists, it should return null.", test);
    }

    @Test
    public void emptyInputGivesNull() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("");
        assertNull("If input is \"\", it should return null.", test);
    }

    @Test
    public void nullInputGivesNull() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient(null);
          assertNull("If input is \"\", it should return null.", test);
    }

    @Test
    public void amountAndPrefix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("2 revet gulerødder");
        RecipeIngredient reference = new RecipeIngredient("2 revet", new Ingredient("gulerod", "gulerødder", false), "");
        assertEquals("It should be possible to have amount and prefix with an ingredient.", reference ,test);
    }

    @Test
    public void matchingPluralBeforeSingularAndSuffix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("2 store citroner");
        RecipeIngredient reference = new RecipeIngredient("2 store", new Ingredient("citron", "citroner", false), "");
        assertEquals("If the name is in plural but the singular name is a prefix to the plural name, it should still pick the plural.", reference ,test);
    }

    @Test
    public void matchingWholeWordBeforePrefix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("en hel del citronskal");
        RecipeIngredient reference = new RecipeIngredient("en hel del ", new Ingredient("citronskal", "citronskal", false), "");
        assertEquals("If a prefix of a ingredient matches another ingredient, it should still use the whole word and not just the prefix.", reference ,test);
    }

    @Test
    public void unknownIngredientWithIngredientAsSuffix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("dewcitronskal");
        RecipeIngredient reference = null;
        assertEquals("If there is a unknown ingredient which contains a suffix of another ingredient, it should return null", reference ,test);
    }

    @Test
    public void unknownIngredientWithIngredientAsPrefix() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("citronskaldew");
        RecipeIngredient reference = null;
        assertEquals("If there is a unknown ingredient which contains a prefix of another ingredient, it should return null", reference ,test);
    }

    @Test
    public void matchingUnitInWord() throws NotConfiguredException, DataStoreException {
        RecipeIngredient test = RecipeIngredientFactory.makeRecipeIngredient("meget citronskal");
        RecipeIngredient reference = new RecipeIngredient("meget", new Ingredient("citronskal", "citronskal", false), "");
        assertEquals("If a word contains a sequence that also matches a unit, that sequence should not be treated as a unit.", reference ,test);
    }
}
