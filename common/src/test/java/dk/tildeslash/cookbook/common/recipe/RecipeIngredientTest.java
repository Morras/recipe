package dk.tildeslash.cookbook.common.recipe;

import org.junit.*;
import static org.junit.Assert.*;
import com.gargoylesoftware.base.testing.EqualsTester;
/*
 * The idea is to to take some
 * ingredients as they show up in
 * a real recipeIngredient and see if the 
 * representation is correct.
 */

public class RecipeIngredientTest {

    private Ingredient vand = new Ingredient("vand", "vand", true);
    private RecipeIngredient recipeIngredientVand = new RecipeIngredient("1 dl", vand, "");
    private RecipeIngredient recipeIngredientVandDuplicate = new RecipeIngredient("1 dl", vand, "");

    private Ingredient selleri = new Ingredient("bladselleri", "bladselleri", false);
    private RecipeIngredient recipeIngredientSelleri = new RecipeIngredient("3 stilke", selleri, "i tynde skiver (ca. 4 dl)");
    private RecipeIngredient recipeIngredientSelleriNullPrefix = new RecipeIngredient(null, selleri, "i tynde skiver (ca. 4 dl)");
    private RecipeIngredient recipeIngredientSelleriNullSuffix = new RecipeIngredient("3 stilke", selleri, null);


    private Ingredient citronskal = new Ingredient("citronskal", "citronskal", false);
    private RecipeIngredient recipeIngredientCitronskal = new RecipeIngredient("4 tsk fintrevet", citronskal, "");

    private Ingredient peber = new Ingredient("peber", "peber", true);
    private RecipeIngredient recipeIngredientPeber = new RecipeIngredient("friskkværnet", peber, "");

    private RecipeIngredient recipeIngredientHalvVand = new RecipeIngredient("0.5 dl", vand, "");

    private RecipeIngredient subclass = new RecipeIngredient("2 dl", vand, ""){};
    
    private RecipeIngredient recipeIngredientNonScalingSuffix = new RecipeIngredient("2 dl", vand, "{5}% fat");

    @Test
    public void testRecipeIngredientVand(){
        assertEquals("recipeIngredientIngredientVand.getIngredientString should yield '1 dl vand'", "1 dl vand", recipeIngredientVand.getIngredientString());
    }

    @Test
    public void testRecipeIngredientSelleri(){
        assertEquals("recipeIngredientSelleri.getIngredientString should yield '3 stilke bladselleri i tynde skiver (ca. 4 dl)",
                "3 stilke bladselleri i tynde skiver (ca. 4 dl)",
                recipeIngredientSelleri.getIngredientString());
    }


    @Test
    public void testRecipeIngredientSelleriLargerPortion(){
        assertEquals("recipeIngredientSelleri.getIngredientString should yield '4.5 stilke bladselleri i tynde skiver (ca. 6 dl)",
                "4.5 stilke bladselleri i tynde skiver (ca. 6 dl)",
                recipeIngredientSelleri.getIngredientString(1.5f));
    }

    @Test
    public void testRecipeIngredientSelleriSmallerPortion(){
        assertEquals("recipeIngredientSelleri.getIngredientString should yield '1.5 stilke bladselleri i tynde skiver (ca. 2 dl)",
                "1.5 stilke bladselleri i tynde skiver (ca. 2 dl)",
                recipeIngredientSelleri.getIngredientString(0.5f));
    }

    @Test
    public void testRecipeIngredientCitronskal(){
        assertEquals("recipeIngredientCitronskal.getIngredientString should yeild '4 tsk fintrevet citronskal'",
                "4 tsk fintrevet citronskal",
                recipeIngredientCitronskal.getIngredientString());
    }

    @Test
    public void testRecipeIngredientPeber(){
        assertEquals("recipeIngredientPeber.getIngredientString should yeild 'Friskkværnet peber' with capital F",
                "Friskkværnet peber",
                recipeIngredientPeber.toString());
    }

    @Test
    public void testRecipeIngredientHalfVand(){
        assertEquals("recipeIngredientVand.getIngredientString should yield '0.5 dl vand'", "0.5 dl vand", recipeIngredientHalvVand.toString());
    }

    @Test
    public void testNullAsPrefixGivesEmptyPrefix(){
        assertEquals("null as prefix should be converted to an empty string",
                "bladselleri i tynde skiver (ca. 4 dl)",
                recipeIngredientSelleriNullPrefix.getIngredientString());
    }

    @Test
    public void testNullAsSuffixGivesEmptyPrefix(){
        assertEquals("null as prefix should be converted to an empty string",
                "3 stilke bladselleri",
                recipeIngredientSelleriNullSuffix.getIngredientString());
    }

    @Test
    public void testEquality(){
        new EqualsTester(recipeIngredientVand, recipeIngredientVandDuplicate, recipeIngredientCitronskal, subclass);
    }

    @Test
    public void testNonScalingSuffix(){
        assertEquals("numbers with {} around them should not scale",
                "4 dl vand 5% fat",
                recipeIngredientNonScalingSuffix.getIngredientString(2));
    }
}
