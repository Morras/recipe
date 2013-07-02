package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.recipe.RecipeIngredient;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class IngredientPanel extends JPanel implements AddIngredientListener{

    private JList<RecipeIngredient> ingredientList;
    private JButton addIngredient;
    private JButton removeIngredient;
    private JButton editIngredient;
    private IngredientListModel listModel = new IngredientListModel();
    private Set<ChangeListener> changeSubscribers = new HashSet<>();

    public IngredientPanel(){
        super();
        initializeComponents();
        setBorders();
        combineComponents();
    }

    private void initializeComponents(){
        ingredientList = new JList<>(listModel); // TODO find out how to change the renderer
        ingredientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ingredientList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (ingredientList.getSelectedIndex() == -1) {
                        addIngredientClicked();
                    } else {
                        editIngredientClicked();
                    }
                }
            }
        });
        ingredientList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeIngredientClicked();
                }
            }
        });

        addIngredient = new JButton("Add Ingredient");
        addIngredient.setFont(new Font("sansserif", Font.BOLD, 11));
        addIngredient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addIngredientClicked();
            }
        });

        removeIngredient = new JButton("Remove Ingredient");
        removeIngredient.setFont(new Font("sansserif", Font.BOLD, 11));
        removeIngredient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeIngredientClicked();
            }
        });

        editIngredient = new JButton("Edit Ingredient");
        editIngredient.setFont(new Font("sansserif", Font.BOLD, 11));
        editIngredient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editIngredientClicked();
            }
        });

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(250, 200));
    }

    private void editIngredientClicked() {
        int selected = ingredientList.getSelectedIndex();
        if ( selected != -1 ) {
            RecipeIngredient oldIngredient = ingredientList.getSelectedValue();
            CreateRecipeIngredientDialog dialog = new CreateRecipeIngredientDialog(oldIngredient);

            dialog.addAddIngredientListener(this);

            listModel.remove(oldIngredient);
            ingredientList.updateUI();
            JOptionPane.showMessageDialog(null, "The ingredient has been removed, and you can now add it again",
                    "Editing Ingredient", JOptionPane.INFORMATION_MESSAGE);

            dialog.showDialog(findParent());
            requestFocus();
        }
    }

    private void removeIngredientClicked() {
        int selected = ingredientList.getSelectedIndex();
        if( selected != -1 ){
            listModel.remove(ingredientList.getSelectedIndex());
            ingredientList.revalidate();
            ingredientList.repaint();
            notifyChangeSubscribers();
        }
    }

    private void addIngredientClicked() {
        CreateRecipeIngredientDialog dialog = new CreateRecipeIngredientDialog();
        dialog.addAddIngredientListener(this);
        dialog.showDialog(findParent());
        requestFocus();
    }

    private Container findParent(){

        Container parent = this;
        while(parent.getParent() != null){
            parent = parent.getParent();
        }

        return parent;
    }

    private void combineComponents(){
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        JScrollPane scrollPane = new JScrollPane(ingredientList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(addIngredient);
        buttonPanel.add(editIngredient);
        buttonPanel.add(removeIngredient);

    }

    private void setBorders(){
        this.setBorder(BorderFactory.createMatteBorder(0, 3, 3, 3, this.getBackground()));
        ingredientList.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, this.getBackground()));
        ingredientList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    }

    public java.util.List<RecipeIngredient> getIngredients(){
        return ((IngredientListModel)ingredientList.getModel()).getIngredients();
    }

    //Sends messages to all subscribers that the
    private void notifyChangeSubscribers(){
        for(ChangeListener subscriber: changeSubscribers){
            subscriber.changed();
        }
    }

    public void addChangeListener(ChangeListener subscriber){
        changeSubscribers.add(subscriber);
    }

    public void addIngredient(RecipeIngredient ingredient){
        listModel.add(ingredient);
        ingredientList.updateUI();
        notifyChangeSubscribers();
    }

    @Override
    public void addIngredientEvent(RecipeIngredient ingredient){
        addIngredient(ingredient);
    }
}

