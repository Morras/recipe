package dk.tildeslash.cookbook.viewer;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.ConnectionException;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;
import dk.tildeslash.cookbook.editor.Editor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 25/06/12
 * Time: 15:53
 */
public class RecipePanel extends JPanel {

    private Recipe recipe;
    private JTextPane ingredientPane = new JTextPane();
    JScrollPane ingredientScroll = new JScrollPane(ingredientPane);
    private JTextPane textPane = new JTextPane();
    JScrollPane textScroll = new JScrollPane(textPane);
    private JTextPane infoPane = new JTextPane();
    private JLabel portionsLabel = new JLabel();
    private int portions;
    private Color backgroundColor = Color.WHITE;

    List<ActionListener> listeners = new LinkedList<>();
    public RecipePanel(Recipe recipe){

        setupComponents();
        loadRecipe(recipe);
    }

    private void setupComponents(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setBackground(backgroundColor);

        textScroll.setBorder(BorderFactory.createEmptyBorder());
        ingredientScroll.setBorder(BorderFactory.createEmptyBorder());

        configureEditorPane(infoPane);
        add(infoPane, BorderLayout.NORTH);
        infoPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY ),
                BorderFactory.createEmptyBorder(0, 0, 0, 10)));

        //Components for changing the number of portions
        JButton minusButton = new JButton("-");
        JButton plusButton = new JButton("+");
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decrementPortions();
            }
        });
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                incrementPortions();
            }
        });
        portionsLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel portionPanel = new JPanel(new BorderLayout());
        portionPanel.add(minusButton, BorderLayout.WEST);
        portionPanel.add(portionsLabel, BorderLayout.CENTER);
        portionPanel.add(plusButton, BorderLayout.EAST);
        portionPanel.setBackground(backgroundColor);
        portionPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        //Ingredient panel
        JPanel ingredientPanel = new JPanel(new BorderLayout());
        ingredientPanel.add(portionPanel, BorderLayout.NORTH);
        ingredientScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ingredientPanel.add(ingredientScroll, BorderLayout.CENTER);
        ingredientPane.setPreferredSize(new Dimension(190, 620));
        ingredientPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
        add(ingredientPanel, BorderLayout.WEST);
        configureEditorPane(ingredientPane);

        textScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(textScroll, BorderLayout.CENTER);
        textPane.setPreferredSize(new Dimension(440, 620));
        configureEditorPane(textPane);

        //Footer pane with buttons
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for( ActionListener listener: listeners){
                    listener.actionPerformed(new ActionEvent(RecipePanel.this, 0, ""));
                }
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRecipe();
            }
        });
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Editor(recipe);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createHorizontalGlue());
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private void refreshRecipe() {
        Recipe refreshedRecipe;
        try{
            DataStoreConnector db = MySQLConnector.getInstance();
            refreshedRecipe = db.retrieveRecipeMatchingName(recipe.getName());
        } catch (NotConfiguredException | DataStoreException | ConnectionException e){
            showDatabaseErrorMessage();
            return;
        }

        loadRecipe(refreshedRecipe);
    }

    private void loadRecipe(Recipe recipe){
        this.recipe = recipe;
        portions = recipe.getPortions();
        loadInformation();
        loadIngredients();
        loadText();
    }

    private void loadIngredients(){
        loadIngredients(recipe.getPortions());
    }

    private void loadIngredients(int portions){
        this.portions = portions;
        updatePortionsLabel();

        ingredientPane.setText("");
        StyledDocument doc = ingredientPane.getStyledDocument();
        doc.setLogicalStyle(doc.getLength(), doc.getStyle("right"));

        for ( RecipeSection section : recipe.getSections() ) {
            try{
                String headline = section.getHeadline();
                if( headline != null && ! headline.equals("") && section.getIngredients().size() != 0){
                    doc.insertString(doc.getLength(), headline + "\n", doc.getStyle("bold"));
                }

                for (RecipeIngredient ingredient : section.getIngredients() ){
                    float scale = (float)portions / recipe.getPortions();
                    doc.insertString(doc.getLength(), ingredient.getIngredientString(scale) + "\n", null);
                }

                doc.insertString(doc.getLength(), "\n", null);

            } catch (BadLocationException e){
                //As long as we use doc.getLength() this exception should not be possible
                e.printStackTrace();
            }
        }

        ingredientPane.setCaretPosition(0);
    }

    private void updatePortionsLabel(){
        if(portions == 1){
            portionsLabel.setText("1 portion");
        } else {
            portionsLabel.setText(portions + " portions");
        }
    }

    private void incrementPortions(){
        portions++;
        loadIngredients(portions);
    }

    private void decrementPortions(){
        if ( portions == 1 ){
            return;
        }
        portions--;
        loadIngredients(portions);
    }

    private void loadText(){
        textPane.setText("");

        StyledDocument doc = textPane.getStyledDocument();
        for ( RecipeSection section: recipe.getSections() ){
            try{
                String headline = section.getHeadline();
                if( headline != null && ! headline.equals("") ){
                    doc.insertString(doc.getLength(), headline + "\n", doc.getStyle("bold"));
                }

                doc.insertString(doc.getLength(), section.getText() + "\n\n", null);
            } catch (BadLocationException e){
                //As long as we use doc.getLength() this exception should not be possible
                e.printStackTrace();
            }
        }

        textPane.setCaretPosition(0);
    }

    private void loadInformation(){

        infoPane.setText("");

        try{
            StyledDocument doc = infoPane.getStyledDocument();
            doc.setLogicalStyle(0, doc.getStyle("right"));
            doc.insertString(doc.getLength(), "Source: " + recipe.getSource() + "\n", null);
            doc.insertString(doc.getLength(), "Calories: " + recipe.getCalories() + "\n", null);
            doc.insertString(doc.getLength(), "Time: " + recipe.getTime() + " minutes", null);
        } catch (BadLocationException e){
            //Since we use doc.getLength() this should never happen
            e.printStackTrace();
        }
    }

    public void addBackListener(ActionListener listener){
        listeners.add(listener);
    }

    private void configureEditorPane(JTextPane pane){
        pane.setFocusable(false);
        pane.setEditable(false);
        pane.setAutoscrolls(true);
        pane.setOpaque(true);
        pane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        StyledDocument doc = pane.getStyledDocument();

        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style main = doc.addStyle("main", defaultStyle);
        StyleConstants.setFontFamily(main, "Serif");
        StyleConstants.setFontSize(main, 14);

        Style styleRef = doc.addStyle("left", main);
        StyleConstants.setAlignment(styleRef, StyleConstants.ALIGN_LEFT);

        styleRef = doc.addStyle("right", main);
        StyleConstants.setAlignment(styleRef, StyleConstants.ALIGN_RIGHT);

        styleRef = doc.addStyle("bold", main);
        StyleConstants.setBold(styleRef, true);

        doc.setLogicalStyle(0, doc.getStyle("main"));
    }

    private void showDatabaseErrorMessage(){
        JOptionPane.showMessageDialog(this,
                "Could not connect to the database, please close the program, check your connection and try again.",
                "Database error",
                JOptionPane.ERROR_MESSAGE);
    }
}
