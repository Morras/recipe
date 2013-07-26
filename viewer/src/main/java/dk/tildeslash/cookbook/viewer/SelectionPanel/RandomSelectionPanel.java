package dk.tildeslash.cookbook.viewer.SelectionPanel;

import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.viewer.SearchCompletedListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 26/06/12
 * Time: 16:38
 */
public class RandomSelectionPanel implements SelectionPanel{

    private List<SearchCompletedListener> listeners = new LinkedList<>();
    private List<Recipe> recipes;
    private JButton randomButton = new JButton("Get random recipe");
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
            showDatabaseErrorMessage();
            System.exit(-1);
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findRandomRecipe();
            }
        });
        panel.add(Box.createHorizontalGlue());
        panel.add(randomButton);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    private void findRandomRecipe(){
        Random random = new Random();
        int r = random.nextInt(recipes.size());

        for ( SearchCompletedListener listener: listeners ){
            listener.searchCompleted(recipes.get(r));
        }
    }


    @Override
    public void addSearchCompletedListener(SearchCompletedListener listener){
        listeners.add(listener);
    }

    @Override
    public String getTitle() {
        return "Select a random recipe";
    }

    @Override
    public void takeFocus() {
        randomButton.requestFocusInWindow();
    }

    private void showDatabaseErrorMessage(){
        JOptionPane.showMessageDialog(panel,
                "Your database appears to be corrupted, was unable to retrieve recipes.",
                "Databse error",
                JOptionPane.ERROR_MESSAGE);
    }
}
