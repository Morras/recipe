package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.editor.ChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashSet;
import java.util.Set;

public class RecipeInformationPanel extends JPanel{
    private JTextField timeField;
    private JTextField sourceField;
    private JTextField caloriesField;
    private JTextField nameField;
    private JTextField portionsField;
    private JPanel mainPanel;

    private Set<ChangeListener> subscribers = new HashSet<>(1);

    public RecipeInformationPanel(){
        super();
        this.add(mainPanel);

        timeField.getDocument().addDocumentListener(new ChangeDocumentListener());
        sourceField.getDocument().addDocumentListener(new ChangeDocumentListener());
        caloriesField.getDocument().addDocumentListener(new ChangeDocumentListener());
        nameField.getDocument().addDocumentListener(new ChangeDocumentListener());
        portionsField.getDocument().addDocumentListener(new ChangeDocumentListener());
    }

    public int getTime() {
        try{
            return Integer.valueOf(timeField.getText());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public void setTime(int minutes) {
        timeField.setText(Integer.toString(minutes));
    }

    public String getSource() {
        return sourceField.getText();
    }

    public void setSource(String source) {
        sourceField.setText(source);
    }

    public int getCalories() {
        try{
            return Integer.valueOf(caloriesField.getText());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public void setCalories(int calories) {
        caloriesField.setText(Integer.toString(calories));
    }

    public String getName() {
        return nameField.getText();
    }

    public void setName(String name) {
        nameField.setText(name);
    }

    public int getPortions(){
        try{
            return Integer.valueOf(portionsField.getText());
        } catch (NumberFormatException e){
            return -1;
        }
    }

    public void setPortions(int portions){
        portionsField.setText(Integer.toString(portions));
    }

    public void addChangeListener(ChangeListener cl){
        subscribers.add(cl);
    }

   void changedEvent(){
        for(ChangeListener cl: subscribers){
            cl.changed();
        }
    }

    private class ChangeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            changedEvent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedEvent();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changedEvent();
        }
    }
}
