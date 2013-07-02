package dk.tildeslash.cookbook.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 16/06/13
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class ViewerKeyboardDispatcher implements KeyEventDispatcher {

    JTabbedPane tabPane;

    public ViewerKeyboardDispatcher(JTabbedPane tabPane){
        this.tabPane = tabPane;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if ( e.getID() == KeyEvent.KEY_RELEASED){
            if ( e.isControlDown() ) {
                switch (KeyEvent.getKeyText(e.getKeyCode())){
                    case "W":
                        tabPane.remove(tabPane.getSelectedIndex());
                        return true;
                    case "Tab":
                        int size = tabPane.getTabCount();
                        int index = tabPane.getSelectedIndex();
                        if ( e.isShiftDown() ){ //Move down the list of tabs
                            index = (index + size - 1) % size;
                        } else { //Move up the list of tabs
                            index = (index + 1) % size;
                        }
                        tabPane.setSelectedIndex(index);
                        return true;
                }
            }
        }
        return false;
    }
}
