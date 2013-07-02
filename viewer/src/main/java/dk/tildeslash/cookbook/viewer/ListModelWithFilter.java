package dk.tildeslash.cookbook.viewer;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 23/06/12
 * Time: 16:32
 */
public class ListModelWithFilter implements ListModel<String>{

    private LinkedList<java.lang.String> store = new LinkedList<>();
    private LinkedList<ListDataListener> dataListeners = new LinkedList<>();
    @Override
    public int getSize() {
        return store.size();
    }

    @Override
    public String getElementAt(int index) {
        return store.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        dataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        dataListeners.remove(l);
    }

    public void addAll(Collection<String> coll){
        store.addAll(coll);
        notifyListeners();
    }

    public void clear(){
        store.clear();
        notifyListeners();
    }

    private void notifyListeners(){
        for( ListDataListener l: dataListeners){
            l.contentsChanged(null);
        }
    }

    /**
     * Removes any String from the list that does not contain the pattern
     * @param pattern the pattern used for matching strings that will stay
     */
    public void filter(String pattern){
        pattern = pattern.toLowerCase();

        ListIterator<String> iterator = store.listIterator(0);

        while ( iterator.hasNext() ) {
            String s = iterator.next();
            s = s.toLowerCase();

            if ( ! s.contains(pattern) ){
                iterator.remove();
                notifyListeners();
            }
        }
    }
}
