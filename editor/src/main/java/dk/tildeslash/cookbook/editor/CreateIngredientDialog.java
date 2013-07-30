package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.ConnectionException;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class CreateIngredientDialog {
    private JCheckBox commonCheckBox;
    private JTextField singularField;
    private JTextField pluralField;
    private JButton cancelButton;
    private JButton OKButton;
    private JPanel mainPanel;

    private JDialog dialog;
    private Ingredient result = null;
    private List<Ingredient> ingredients;
    private DataStoreConnector db;

    private Ingredient oldIngredient = null;

    public CreateIngredientDialog(){
        setupActionListeners();
    }

    public CreateIngredientDialog(Ingredient ingredient){
        this();
        singularField.setText(ingredient.getSingular());
        pluralField.setText(ingredient.getPlural());
        commonCheckBox.setSelected(ingredient.isCommon());
        oldIngredient = ingredient;
    }

    public Ingredient showDialog(Component owner){
        dialog = new JDialog();

        try{
            db = MySQLConnector.getInstance();
            ingredients = db.retrieveAllIngredients();
        } catch (NotConfiguredException  | DataStoreException | ConnectionException e){
            JOptionPane.showMessageDialog(dialog, "There was a problem with connecting to the database,\n" +
                    "The problem is either with the connection or with the configuration file",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        dialog.setLocationRelativeTo(owner);
        dialog.add(mainPanel);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setVisible(true);
        return result;
    }

    private void setupActionListeners(){

        singularField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pluralField.requestFocusInWindow();
            }
        });

        pluralField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        //Let escape close the window.
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            // close the frame when the user presses escape
            public void actionPerformed(ActionEvent e) {
               cancelPressed();
            }
        };
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        mainPanel.getActionMap().put("ESCAPE", escapeAction);
    }

    private void cancelPressed(){
        result = null;
        dialog.dispose();
    }

    private void okPressed(){
        String singular = singularField.getText().trim().toLowerCase();
        String plural = pluralField.getText().trim().toLowerCase();
        if(singular.equals("")){
            JOptionPane.showMessageDialog(dialog, "You need to supply a singular name for the ingredient.\n" +
                    "The two names can be identical", "Invalid name", JOptionPane.ERROR_MESSAGE);

            singularField.requestFocusInWindow();
            return;
        }

        if(plural.equals("")){
            JOptionPane.showMessageDialog(dialog, "You need to supply a plural name for the ingredient.\n" +
                    "The two names can be identical", "Invalid name", JOptionPane.ERROR_MESSAGE);

            pluralField.requestFocusInWindow();
            return;
        }

        Ingredient tmpIngredient = new Ingredient(singular, plural, commonCheckBox.isSelected());

        if(oldIngredient != null){
            db.updateIngredient(oldIngredient, tmpIngredient);
        } else if(ingredients.contains(tmpIngredient)){
            JOptionPane.showMessageDialog(dialog, "There already exists an ingredient with this (singular) name",
                    "Ingredient exists", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        db.addIngredient(tmpIngredient);

        result = tmpIngredient;
        dialog.dispose();
    }
}
