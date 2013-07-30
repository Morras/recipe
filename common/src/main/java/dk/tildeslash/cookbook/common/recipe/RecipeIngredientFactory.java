package dk.tildeslash.cookbook.common.recipe;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.ConnectionException;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.exception.NoFixesFoundException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeIngredientFactory {

    private static final Logger LOGGER = Logger.getLogger(RecipeIngredientFactory.class);

    static public RecipeIngredient makeRecipeIngredient(String input) throws NotConfiguredException, DataStoreException, ConnectionException {
        LOGGER.trace("called: makeRecipeIngredient( \"" + input + "\" )");
        if(input == null){
            LOGGER.warn("input was null, returning null");
            return null;
        }

        input = input.trim().toLowerCase().replaceAll("\\s+", " ");
        if(input.equals("")){
            LOGGER.warn("input was empty, returning null");
            return null;
        }

        DataStoreConnector db = MySQLConnector.getInstance();

        String ingredientName = findIngredientName(input);

        Ingredient ingredient = db.retrieveIngredient(ingredientName);
        if(ingredient == null){
            LOGGER.warn("ingredient was null, returning null");
            return null;
        }

        Map<String, String> fixes;
        try{
            fixes = findPrefixAndSuffix(input, ingredientName);
        } catch (NoFixesFoundException e){
            return new RecipeIngredient("", ingredient, "");
        }
        String prefix = fixes.get("prefix");
        String suffix = fixes.get("suffix");

        return new RecipeIngredient(prefix, ingredient, suffix);
    }

    private static String findIngredientName(String input) throws NotConfiguredException, DataStoreException, ConnectionException {
        LOGGER.trace("called: findIngredientName( \"" + input + "\" )");

        DataStoreConnector db = MySQLConnector.getInstance();
        List<Ingredient> ingredients = db.retrieveAllIngredients();

        String regex = "";
        for(Ingredient i: ingredients){
            regex = regex + "|((^|\\s)" + i.getPlural() + "($|\\s))";
            regex = regex + "|";
            regex = regex + "((^|\\s)" + i.getSingular() + "($|\\s))";
        }
        regex = regex.replaceFirst("\\|", "");

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if( ! matcher.find()){
            LOGGER.trace("found nothing, returning the empty string");
            return "";
        }
        LOGGER.trace("returning '" + matcher.group() + "'");
        return matcher.group().trim();
    }

    private static Map<String, String> findPrefixAndSuffix(String input, String ingredientName) throws NoFixesFoundException{

        Map<String, String> fixes = new HashMap<>(2);
        String[] split = input.split(ingredientName);
        if(split.length == 0){
            throw new NoFixesFoundException();
        } else if(split.length == 1){
            if(input.startsWith(ingredientName)){
                fixes.put("suffix", split[0].trim());
            } else {
                fixes.put("prefix", split[0]);
            }
        } else if (split.length == 2){
            fixes.put("prefix", split[0]);
            fixes.put("suffix", split[1].trim());
        }
        return fixes;
    }
}