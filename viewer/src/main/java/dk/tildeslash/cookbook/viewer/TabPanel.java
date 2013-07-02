package dk.tildeslash.cookbook.viewer;

import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.viewer.SelectionPanel.SelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 25/06/12
 * Time: 15:59
 */
public class TabPanel extends JPanel implements SearchCompletedListener{

    private SelectionPanel selectionPanel;
    private RecipePanel recipePanel;
    private TabComponentWithClose tabComponent;

    public TabPanel(SelectionPanel selectionPanel, TabComponentWithClose tabComponent){
        super();
        setLayout(new BorderLayout());
        this.selectionPanel = selectionPanel;
        this.tabComponent = tabComponent;
        selectionPanel.addSearchCompletedListener(this);
        add(selectionPanel, BorderLayout.CENTER);
    }

    @Override
    public void searchCompleted(Recipe searchResult){
        remove(selectionPanel);
        recipePanel = new RecipePanel(searchResult);
        recipePanel.addBackListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backCalledFromRecipe();
            }
        });
        add(recipePanel, BorderLayout.CENTER);
        tabComponent.setTitle(searchResult.getName());
        updateUI();
    }

    public void backCalledFromRecipe(){
            remove(recipePanel);
            add(selectionPanel);
            tabComponent.setTitle(selectionPanel.getTitle());
            updateUI();
    }
}
