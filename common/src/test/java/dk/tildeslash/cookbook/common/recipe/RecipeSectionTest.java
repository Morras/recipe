package dk.tildeslash.cookbook.common.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.*;

public class RecipeSectionTest {

	private Ingredient ingredient = new Ingredient("vand", "vand", true);
	private RecipeIngredient recipeIngredient = new RecipeIngredient("1 dl", ingredient, "");
	private List<RecipeIngredient> ingredients = Arrays.asList(recipeIngredient);

	private RecipeSection section = new RecipeSection("Headline", "Text", ingredients);
	private RecipeSection sectionSame = new RecipeSection("Headline", "Text", ingredients);
	private RecipeSection sectionDifferentHeadline = new RecipeSection("Different Headline", "Text", ingredients);
	private RecipeSection sectionDifferentText = new RecipeSection("Headline", "Different Text", ingredients);
	private RecipeSection sectionDifferentList = new RecipeSection("Headline", "Different Text", new ArrayList<RecipeIngredient>());
	private RecipeSection sectionSubclass = new RecipeSection("Headline", "Text", new ArrayList<RecipeIngredient>()){};

	@Test
		public void EqualRecipeSectionTestDifferentHeadline(){
			new EqualsTester(section, sectionSame, sectionDifferentHeadline, sectionSubclass);
		}

	@Test
		public void EqualRecipeSectionTestDifferentText(){
			new EqualsTester(section, sectionSame, sectionDifferentText, sectionSubclass);
		}

	@Test
		public void EqualRecipeSectionTestDifferentList(){
			new EqualsTester(section, sectionSame, sectionDifferentList, sectionSubclass);
		}
}
