package dk.tildeslash.cookbook.viewer;

import dk.tildeslash.cookbook.common.recipe.Recipe;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 25/06/12
 * Time: 16:02
 */
public interface SearchCompletedListener {
    public void searchCompleted(Recipe searchResult);
}
