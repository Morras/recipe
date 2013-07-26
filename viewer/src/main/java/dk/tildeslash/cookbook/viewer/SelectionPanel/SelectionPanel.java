package dk.tildeslash.cookbook.viewer.SelectionPanel;

import dk.tildeslash.cookbook.viewer.SearchCompletedListener;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 25/06/12
 * Time: 15:54
 */
public interface SelectionPanel {

    public void addSearchCompletedListener(SearchCompletedListener listener);

    public String getTitle();

    public JPanel getPanel();

    public void takeFocus();
}
