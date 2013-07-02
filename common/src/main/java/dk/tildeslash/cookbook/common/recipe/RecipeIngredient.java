package dk.tildeslash.cookbook.common.recipe;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeIngredient {

    private final Ingredient ingredient;
    private final String prefix;
    private final String suffix;
    private int cachedHash;

    //Pattern will match for decimal numbers e.i. 9 9.0 or 0.9, but only if they are surrounded by non word char or begin/end of line
    //Group 1 and 3 are the chars surrounding the decimal number
    private static Pattern decimalPattern = Pattern.compile("(^|\\W)(\\d+(?:\\.\\d+)?)(\\W|$)");

    private static final Logger LOGGER = Logger.getLogger(RecipeIngredient.class);

    public RecipeIngredient( String prefix, Ingredient ingredient, String suffix){
        this.ingredient = ingredient;
        this.prefix = ( prefix == null ) ? "" : prefix.trim();
        this.suffix = ( suffix == null ) ? "" : suffix.trim();
    }

    public String getPrefix(){
        return prefix;
    }

    public String getSuffix(){
        return suffix;
    }

    public Ingredient getIngredient(){
        return ingredient;
    }

    @Override
    public boolean equals(Object other){
        if(other == null){
            return false;
        } else if (this == other) {
            return true;
        } else if (!(other instanceof RecipeIngredient)) {
            return false;
        }

        //Numbers in prefix and suffix might be sql doubles so we should remove
        //their trailing zeros since they make no semantic difference.
        String ownPrefix = adjustUnitsInString(prefix, 1);
        String otherPrefix = adjustUnitsInString(((RecipeIngredient) other).getPrefix(), 1);
        String ownSuffix = adjustUnitsInString(suffix, 1);
        String otherSuffix = adjustUnitsInString(((RecipeIngredient) other).getSuffix(), 1);
        return ownPrefix.equals(otherPrefix) &&
                ownSuffix.equals((otherSuffix)) &&
                ingredient.equals(((RecipeIngredient) other).getIngredient());
    }

    @Override
    public int hashCode(){
        if(cachedHash == 0){
            cachedHash = (prefix + suffix + ingredient).hashCode();
        }
        return cachedHash;
    }

    @Override
    public String toString(){
        return getIngredientString();
    }

    public String getIngredientString(){
        return getIngredientString(1);
    }

    public String getIngredientString(float scale){

        String returnString;

        String newPrefix = adjustUnitsInString(prefix, scale);
        String newSuffix = adjustUnitsInString(suffix, scale);

        float assumedAmount = 1.0f;
        Matcher matcher = decimalPattern.matcher(newPrefix);

        while ( matcher.find() ){
            String group1 = ( matcher.group(1) == null ) ? "" : matcher.group(1).trim();
            String group3 = ( matcher.group(3) == null ) ? "" : matcher.group(3).trim();
            //Only numbers with no chars around it can be amounts, others are meta data like 70% fat
            if ( group1.isEmpty() && group3.isEmpty() ){
                try{
                    assumedAmount = Float.parseFloat(matcher.group(2));
                    break;
                } catch (NumberFormatException e){
                    LOGGER.error("NumberFormatException thrown while converting '" + matcher.group(2) + "' to float, " +
                            "this should have been caught in the regex. " +
                            "Complete input string: '" + newPrefix);
                }
            }
        }

        //singular no amount
        if(assumedAmount == 0){
            returnString = newPrefix + " " + ingredient.getSingular() + " " + newSuffix;
        }
        //singular
        else if(assumedAmount == 1){
            returnString = newPrefix + " " + ingredient.getSingular() + " " + newSuffix;
        //plural
        } else {
            returnString = newPrefix + " " + ingredient.getPlural() + " " + newSuffix;
        }

        //removes double spaces that happens
        //if unit, prefix or any other item
        //is empty.
        returnString = returnString.replaceAll("\\s+", " ");

        //Remove special chars that is used around numbers not to be scale
        returnString = returnString.replaceAll("\\{|\\}", "");

        //Capitalize the first char to make it look pretty
        String firstChar = returnString.substring(0,1);
        returnString = returnString.replaceFirst(firstChar, firstChar.toUpperCase());

        return returnString.trim();
    }


    private String adjustUnitsInString(String input, float scale){

        StringBuilder output = new StringBuilder(input);
        Matcher matcher = decimalPattern.matcher(input);

        while ( matcher.find() ){
            String group1 = ( matcher.group(1) == null ) ? "" : matcher.group(1).trim();
            String group3 = ( matcher.group(3) == null ) ? "" : matcher.group(3).trim();
            //We only scale numbers that do not have any chars surrounding them
            if ( group1.isEmpty() && group3.isEmpty() ){

                try{
                    float originalAmount = Float.parseFloat(matcher.group(2));
                    float newAmount = originalAmount  * scale;

                    DecimalFormat format = new DecimalFormat();
                    format.setMaximumFractionDigits(2);
                    format.setMinimumFractionDigits(0);
                    format.setGroupingUsed(false);
                    String replacementAmount = format.format(newAmount);


                    int offset = matcher.start() + matcher.group(1).length();
                    output.replace(offset, offset+matcher.group(2).length(), replacementAmount);

                } catch (NumberFormatException e){
                    //We cannot change the value so we ignore it., regex should prevent os from getting here in the first place.
                    LOGGER.error("NumberFormatException thrown while converting '" + matcher.group(2) + "' to float, " +
                            "this should have been caught in the regex. " +
                            "Complete input string: '" + input);
                }
            }
        }

        return output.toString();
    }
}