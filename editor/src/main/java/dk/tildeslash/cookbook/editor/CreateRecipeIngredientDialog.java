package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredientFactory;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class CreateRecipeIngredientDialog {
    private JTextField stringField;
    private JComboBox<Ingredient> ingredientBox;
    private JTextField prefixField;
    private JTextField suffixField;
    private JButton exitButton;
    private JButton AddButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JButton createIngredientButton;
    private JDialog dialog;
    private DefaultComboBoxModel<Ingredient> comboBoxModel;

    private Ingredient defaultIngredient = null;

    private List<AddIngredientListener> addIngredientListeners = new LinkedList<>();

    public CreateRecipeIngredientDialog(RecipeIngredient input) {
        this();
        prefixField.setText(input.getPrefix());
        suffixField.setText(input.getSuffix());
        defaultIngredient = input.getIngredient();
        ingredientBox.setSelectedItem(input.getIngredient());
    }

    public CreateRecipeIngredientDialog() {
        setupActionListeners();
    }

    public void showDialog(Component owner){

        List<Ingredient> ingredients;

        try{
            DataStoreConnector db = MySQLConnector.getInstance();
            ingredients = db.retrieveAllIngredients();
        } catch (NotConfiguredException | DataStoreException e){
            showDataBaseErrorMessage();
            return;
        }
        Collections.sort(ingredients, new IngredientNameComparator());
        comboBoxModel = new SortedMutableComboBoxModel<>(ingredients, new IngredientNameComparator());
        ingredientBox.setModel(comboBoxModel);

        if(defaultIngredient != null){
            ingredientBox.setSelectedItem(defaultIngredient);
        }

        dialog = new JDialog();
        dialog.setLocationRelativeTo(owner);
        dialog.add(mainPanel);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void addAddIngredientListener(AddIngredientListener listener){
        addIngredientListeners.add(listener);
    }

    private void setupActionListeners(){
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitPressed();
            }
        });

        AddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed();
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePressed();
            }
        });

        createIngredientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createIngredientPressed();
            }
        });

        stringField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePressed();
            }
        });

        //Let escape close the window.
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            // close the frame when the user presses escape
            public void actionPerformed(ActionEvent e) {
                exitPressed();
            }
        };
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        mainPanel.getActionMap().put("ESCAPE", escapeAction);
    }


    private void exitPressed(){
        dialog.dispose();
    }

    private void addPressed(){
        Ingredient ingredient = (Ingredient)ingredientBox.getSelectedItem();

        RecipeIngredient result = new RecipeIngredient(prefixField.getText().trim(), ingredient, suffixField.getText().trim());

        for(AddIngredientListener listener: addIngredientListeners){
            listener.addIngredientEvent(result);
        }

        prefixField.setText("");
        ingredientBox.setSelectedIndex(-1);
        suffixField.setText("");

        stringField.selectAll();
        stringField.requestFocusInWindow();
        mainPanel.getRootPane().setDefaultButton(generateButton);
    }

    private void generatePressed(){
        RecipeIngredient tmpResult = null;
        String ingredientString = stringField.getText().trim();
        try{
            tmpResult = RecipeIngredientFactory.makeRecipeIngredient(ingredientString);
        } catch (NotConfiguredException | DataStoreException e){
            showDataBaseErrorMessage();
        }
        if(tmpResult == null){

            //Try and come up with alternatives for the ingredient.
            TreeMap<Integer, List<String>> map = new TreeMap<>();
            List<Ingredient> ingredients;
            try{
                DataStoreConnector db = MySQLConnector.getInstance();
                ingredients = db.retrieveAllIngredients();
            } catch (NotConfiguredException | DataStoreException e){
                showDataBaseErrorMessage();
                return;
            }

            for (Ingredient ingredient : ingredients){

                String singular = ingredient.getSingular();
                String plural = ingredient.getPlural();
                //TODO this is not that good, it works but could be better
                int distance = Math.min(StringUtils.getLevenshteinDistance(ingredientString, singular),
                        StringUtils.getLevenshteinDistance(ingredientString, plural));

                if ( ingredientString.contains(singular) || ingredientString.contains(plural) ||
                        singular.contains(ingredientString) || plural.contains(ingredientString)){
                    distance = 1;
                }

                if ( ! map.containsKey(distance) ){
                    map.put(distance, new LinkedList<String>());
                }
                map.get(distance).add(ingredient.getSingular());
            }

            String message = "Unable to generate an ingredient from the input.\n" +
                    "This is usually because the base ingredient was not found. \n" +
                    "Other reasons might be that you supplied an unit but no amount.\n\n" +
                    "Here are the 5 most likely ingredients based on your input: \n";

            Set<Integer> keySet = map.keySet();
            int count = 0;
            for ( Integer i : keySet){
                for (String singular : map.get(i)){
                    if( count >= 5 ){
                        break;
                    }
                    message += "\t" + singular + "\n";
                    count++;
                }
                if( count >= 5 ){
                    break;
                }
            }

            JOptionPane.showMessageDialog(dialog, message,
                    "Ingredient generation error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        prefixField.setText(tmpResult.getPrefix());
        ingredientBox.setSelectedItem(tmpResult.getIngredient());
        suffixField.setText(tmpResult.getSuffix());

        AddButton.requestFocusInWindow();
        mainPanel.getRootPane().setDefaultButton(AddButton);
    }

    private void createIngredientPressed(){
        CreateIngredientDialog ingredientDialog = new CreateIngredientDialog();
        Ingredient ingredient = ingredientDialog.showDialog(dialog);
        if(ingredient != null){
            comboBoxModel.addElement(ingredient);
            comboBoxModel.setSelectedItem(ingredient);
            generateButton.requestFocusInWindow();
        }
    }

    private void showDataBaseErrorMessage(){
        JOptionPane.showMessageDialog(dialog, "There was a problem with connecting to the database,\n" +
                "The problem is either with the connection or with the configuration file",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private class IngredientNameComparator implements Comparator<Ingredient> {
        @Override
        public int compare(Ingredient ingredient1, Ingredient ingredient2) {
            return ingredient1.getSingular().compareTo(ingredient2.getSingular());
        }
    }
}

