package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class RecipeSectionPanel extends JPanel implements ChangeListener{

    private IngredientPanel ingredientPanel;
    private JTextArea textPane;
    private JPanel textPanel;
    private JTextField headline;
    private	JPanel headlinePanel = new JPanel(new GridLayout());
    private JPanel textPaneBorder = new JPanel(new GridLayout());
    private JPanel leftPanel = new JPanel(new BorderLayout());
    private JScrollPane textScrollPane;
    private Set<ChangeListener> changeSubscribers = new HashSet<>(1);

    //The number of this section in the recipe
    //i.e. first section is number 1, second is 2 and so forth.
    private int sectionNumber;

    public RecipeSectionPanel(RecipeSection section, int sectionNumber){
        this(sectionNumber);
        headline.setText(section.getHeadline());
        textPane.setText(section.getText());
        for(RecipeIngredient ingredient: section.getIngredients()){
            ingredientPanel.addIngredient(ingredient);
        }
    }

    public RecipeSectionPanel(int sectionNumber){
        super();
        this.sectionNumber = sectionNumber;
        initializeComponents();
        setBorders();
        combineComponents();
    }

    private void initializeComponents(){

        this.setLayout(new BorderLayout());

        ingredientPanel = new IngredientPanel();
        ingredientPanel.addChangeListener(this);
        textPanel = new JPanel(new BorderLayout());
        textPane = new JTextArea();
        textPane.setLineWrap(true);

        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        textPane.setEditable(true);
        textPane.getDocument().addDocumentListener(new ChangeDocumentListener());
        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScrollPane.setPreferredSize(new Dimension(450, 200));

        headline = new JTextField();
        headline.getDocument().addDocumentListener(new ChangeDocumentListener());

        this.setPreferredSize(new Dimension(750, 250));
    }

    private void combineComponents(){
        this.add(leftPanel, BorderLayout.WEST);
        leftPanel.add(ingredientPanel, BorderLayout.CENTER);
        leftPanel.add(headlinePanel, BorderLayout.NORTH);

        textPaneBorder.add(textScrollPane);

        this.add(textPanel, BorderLayout.CENTER);
        headlinePanel.add(headline);
        textPanel.add(textPaneBorder, BorderLayout.CENTER);
    }

    private void setBorders(){
        this.setBorder(createMainBorder());
        textPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        headline.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        headlinePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "Optional section headline",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("sansserif", Font.PLAIN, 10)));
        textPaneBorder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "Text",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("sansserif", Font.PLAIN, 10)));
    }

    public void addChangeListener(ChangeListener cl){
        changeSubscribers.add(cl);
    }

    public void changed(){
        for(ChangeListener cl : changeSubscribers){
            cl.changed();
        }
    }

    public int getSectionNumber(){
        return sectionNumber;
    }

    public void setSectionNumber(int newNumber){
        sectionNumber = newNumber;
        this.setBorder(createMainBorder());
    }

    private Border createMainBorder(){
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK), "Section " + sectionNumber);
    }

    public RecipeSection getRecipeSection(){
        return new RecipeSection(headline.getText(), textPane.getText(),  ingredientPanel.getIngredients());
    }

    private class ChangeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed();
        }
    }
}

