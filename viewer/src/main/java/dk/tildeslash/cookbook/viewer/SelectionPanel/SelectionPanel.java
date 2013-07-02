package dk.tildeslash.cookbook.viewer.SelectionPanel;

import dk.tildeslash.cookbook.viewer.SearchCompletedListener;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 25/06/12
 * Time: 15:54
 */
public abstract class SelectionPanel extends JPanel {

    public abstract void addSearchCompletedListener(SearchCompletedListener listener);

    public abstract String getTitle();

    public abstract void takeFocus();
}
