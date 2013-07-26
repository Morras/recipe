package dk.tildeslash.cookbook.viewer.SelectionPanel;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;
import dk.tildeslash.cookbook.viewer.SearchCompletedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 26/06/12
 * Time: 18:48
 */
public class IngredientMatchingSelectionPanel implements SelectionPanel{
    private JPanel mainPanel;
    private JTextField ingredientTextField;
    private JLabel ingredientLabel;
    private JButton addIngredientButton;
    private JPanel cbPanel1;
    private JPanel cbPanel2;
    private JPanel cbPanel3;
    private JList<Recipe> recipeList;
    private JButton findButton;
    private JButton clearButton;

    private DataStoreConnector db;

    private DefaultListModel<Recipe> listModel = new DefaultListModel<>();

    private List<SearchCompletedListener> listeners = new LinkedList<>();
    private List<CheckBoxWithIngredient> checkBoxes = new LinkedList<>();
    private Set<Ingredient> customIngredients = new HashSet<>();
    private List<Recipe> allRecipes;

    private JPanel panel;

    @Override
    public JPanel getPanel(){

        try {
            db = MySQLConnector.getInstance();
            allRecipes = db.retrieveAllRecipes();
        } catch (NotConfiguredException e) {
            showErrorMessage("Unable to connect to the database", "Database Error");
            System.exit(-1);
        } catch (DataStoreException e) {
            showErrorMessage("Unable to retrieve recipes. Database appears corrupted.", "Database Error");
            System.exit(-1);
        }

        setupCheckboxes();
        setupListeners();

        panel = new JPanel();
        recipeList.setModel(listModel);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());
        panel.add(mainPanel, BorderLayout.CENTER);

        return panel;
    }

    private void setupListeners() {

        ingredientTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomIngredient();
            }
        });

        addIngredientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomIngredient();
            }
        });

        recipeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    notifySearchListeners();
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customIngredients.clear();
                ingredientLabel.setText("");
            }
        });

        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findRecipes();
            }
        });
    }

    private void addCustomIngredient(){
        Ingredient ingredient;
        try{
            ingredient = db.retrieveIngredient(ingredientTextField.getText());
        } catch (DataStoreException e){
            showErrorMessage("There was a problem retrieving the ingredient from the data store", "Data store error");
            return;
        }
        if( ingredient == null ){
            showErrorMessage("Could not find an ingredient called '" + ingredientTextField.getText() + "'", "Unknown ingredient" );
            return;
        }
        if( ingredient.isCommon() ){
            showErrorMessage("The ingredient (" + ingredientTextField.getText() + ") is considered a common item and is not searchable'", "Common ingredient" );
            return;
        }

        if ( customIngredients.add(ingredient) ){
            ingredientLabel.setText(ingredientLabel.getText() + ingredient.getSingular() + "; ");
        }
    }

    private void findRecipes() {
        listModel.clear();

        TreeMap<Integer, SortedSet<Recipe>> recipeMap = new TreeMap<>();

        Set<Ingredient> matchingIngredients = new  HashSet<>(customIngredients);
        for( CheckBoxWithIngredient cb : checkBoxes ){
            if ( cb.isChecked() ){
                matchingIngredients.add(cb.getIngredient());
            }
        }

        for (Recipe recipe : allRecipes ){

            Set<Ingredient> recipeIngredients = new HashSet<>();
            for (RecipeSection section : recipe.getSections() ){
                for(RecipeIngredient rIngredient : section.getIngredients() ){
                    Ingredient ingredient = rIngredient.getIngredient();
                    if( ! ingredient.isCommon() ){
                        recipeIngredients.add(ingredient);
                    }
                }
            }

            recipeIngredients.removeAll(matchingIngredients);
            int numberOfMissingIngredients = recipeIngredients.size();
            if ( recipeMap.get(numberOfMissingIngredients) == null ){
                recipeMap.put(numberOfMissingIngredients, new TreeSet<Recipe>());
            }
            recipeMap.get(numberOfMissingIngredients).add(recipe);
        }

        for ( int i : recipeMap.keySet() ){
            for( Recipe recipe: recipeMap.get(i)){
                listModel.addElement(recipe);
            }
        }

        int leastMissingIngredients = recipeMap.firstKey();
        Recipe bestRecipe = recipeMap.get(leastMissingIngredients).first();

        String message = "The best matching recipe is '" + bestRecipe + "' which is missing " + leastMissingIngredients + " ingredient";
        if(leastMissingIngredients != 1 ) {
            message = message + "s";
        }

        JOptionPane.showMessageDialog(panel, message, "Best matching recipe", JOptionPane.INFORMATION_MESSAGE);
    }

    private void notifySearchListeners() {
        Recipe recipe = recipeList.getSelectedValue();
        if( recipe != null){
            for (SearchCompletedListener listener : listeners){
                listener.searchCompleted(recipe);
            }
        }
    }

    private void setupCheckboxes(){
        //Make sure to call this with a limit that is divisible by 3
        List<Ingredient> ingredients;
        try{
            ingredients = db.retrieveMostPopularIngredients(21, false);
        } catch (DataStoreException e){
            showErrorMessage("There was a problem retrieving ingredients from the data store", "Data store error");
            return;
        }
        int limit = ingredients.size();
        int partialLimit = limit/3;

        addCheckboxesToPanel(ingredients, 0, partialLimit, cbPanel1);
        addCheckboxesToPanel(ingredients, partialLimit, partialLimit*2, cbPanel2);
        addCheckboxesToPanel(ingredients, partialLimit*2, limit, cbPanel3);
    }

    private void addCheckboxesToPanel(List<Ingredient> ingredients, int start, int end, JPanel panel){
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for( int i = start; i < end; i++){
            Ingredient ingredient = ingredients.get(i);
            CheckBoxWithIngredient cb = new CheckBoxWithIngredient(ingredient, ingredient.getSingular());
            checkBoxes.add(cb);
            panel.add(cb);
        }
    }

    @Override
    public void addSearchCompletedListener(SearchCompletedListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getTitle() {
        return "Search for Recipes matching ingredients";
    }

    @Override
    public void takeFocus() {
        ingredientTextField.requestFocusInWindow();
    }

    private class CheckBoxWithIngredient extends JCheckBox{

        Ingredient ingredient;
        boolean isChecked = false;

        CheckBoxWithIngredient(Ingredient ingredient, String label){
            super(label);
            this.ingredient = ingredient;

            addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    isChecked = ! isChecked;
                }
            });
        }

        Ingredient getIngredient(){
            return ingredient;
        }

        boolean isChecked(){
            return isChecked;
        }
    }

    private void showErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(panel, message, title, JOptionPane.ERROR_MESSAGE);
    }
}


