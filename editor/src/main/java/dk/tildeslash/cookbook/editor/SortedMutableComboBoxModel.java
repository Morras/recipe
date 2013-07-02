package dk.tildeslash.cookbook.editor;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedMutableComboBoxModel<E> extends DefaultComboBoxModel<E>{

    private List<E> elements;
    private Comparator<E> comparator;

    public SortedMutableComboBoxModel(List<E> startElements, Comparator<E> comparator){
        elements = new ArrayList<>(startElements.size()+5);
        elements.addAll(startElements);
        this.comparator = comparator;
        sort();
    }

    @Override
    public void addElement(E item) {
        elements.add(item);
        sort();

        notifyListeners(ListDataEvent.INTERVAL_ADDED, elements.indexOf(item));
    }

    private void notifyListeners(int notificationEvent, int startIndex){
        ListDataListener[] listeners = getListDataListeners();
        for (ListDataListener listener : listeners) {
            //noinspection MagicConstant
            listener.contentsChanged(new ListDataEvent(this, notificationEvent, startIndex, startIndex + 1));
        }
    }

    @Override
    public void removeElement(Object obj) {
        @SuppressWarnings("SuspiciousMethodCalls")
        int index = elements.indexOf(obj) -1;
        removeElementAt(index);
    }

    @Override
    public void insertElementAt(E item, int index) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Cannot insert element at a specifik index " +
                "since sorting will break that contract later");
    }

    @Override
    public void removeElementAt(int index) {
        elements.remove(index);
        notifyListeners(ListDataEvent.INTERVAL_REMOVED, index);
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public E getElementAt(int index) {
        return elements.get(index);
    }

    private void sort(){
        Collections.sort(elements, comparator);
    }
}
