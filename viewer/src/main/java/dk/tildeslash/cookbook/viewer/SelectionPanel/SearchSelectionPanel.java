package dk.tildeslash.cookbook.viewer.SelectionPanel;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.viewer.ListModelWithFilter;
import dk.tildeslash.cookbook.viewer.SearchCompletedListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 23/06/12
 * Time: 15:39
 */
public class SearchSelectionPanel implements SelectionPanel{

    private JTextField nameField = new JTextField();
    private JList<String> recipeList = new JList<>();
    private JScrollPane scrollPane = new JScrollPane(recipeList);

    private ListModelWithFilter listModel = new ListModelWithFilter();

    private List<Recipe> recipes;
    private List<String> recipeNames;

    private List<SearchCompletedListener> listeners = new LinkedList<>();

    private JPanel panel;

    @Override
    public JPanel getPanel(){
        if ( panel != null ){
            return panel;
        }

        try{
            DataStoreConnector db = MySQLConnector.getInstance();
            recipes = db.retrieveAllRecipes();
        } catch (DataStoreException | NotConfiguredException e){
            showErrorMessage("Problem connecting to the database, it may be corrupt.",
                    "Corrupted datastore");
            System.exit(-1);
        }

        recipeNames = retrieveRecipeNames(recipes);

        panel = new JPanel();
        setupComponents();
        setupListeners();

        return panel;
    }

    private void setupComponents(){
        panel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(new JLabel("Name:"), BorderLayout.NORTH);
        headerPanel.add(nameField, BorderLayout.CENTER);
        nameField.requestFocusInWindow();

        headerPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.black));

        panel.add(headerPanel, BorderLayout.NORTH);

        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listModel.addAll(recipeNames);
        recipeList.setModel(listModel);
        recipeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if ( e.getClickCount() > 1 ){
                    notifySearchListeners();
                }
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners(){
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            //For inserts we can just filter the names already in the list
            //since any names that are filtered out would still be left out of a new complete filtering.
            @Override
            public void insertUpdate(DocumentEvent e) {
                listModel.filter(nameField.getText());
            }

            //Since we have removed a letter we need to redo the filtering of all names
            @Override
            public void removeUpdate(DocumentEvent e) {
                listModel.clear();
                listModel.addAll(recipeNames);
                listModel.filter(nameField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void notifySearchListeners(){
        String recipeName = recipeList.getSelectedValue();
        boolean match = false;
        Recipe foundRecipe = null;

        for( Recipe recipe : recipes ){
            if ( recipe.getName().equals(recipeName)){
                foundRecipe = recipe;
                match = true;
                break;
            }
        }

        if ( match ){
            for ( SearchCompletedListener listener: listeners ){
                listener.searchCompleted(foundRecipe);
            }
        } else {
            showErrorMessage("The recipe matching \"" + recipeName + "\" is no longer available, try and open up a new search.",
                    "Recipe unavailable");
        }
    }

    private List<String> retrieveRecipeNames(List<Recipe> recipes){
        List<String> result = new LinkedList<>();
        for ( Recipe r: recipes ){
            result.add(r.getName());
        }

        return result;
    }

    private void showErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(panel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void addSearchCompletedListener(SearchCompletedListener listener){
        listeners.add(listener);
    }

    @Override
    public String getTitle() {
        return "Search for a recipe by name";
    }

    @Override
    public void takeFocus() {
        nameField.requestFocusInWindow();
    }
}