package dk.tildeslash.cookbook.editor;

import dk.tildeslash.cookbook.common.datastore.Configuration;
import dk.tildeslash.cookbook.common.datastore.DataStoreConnector;
import dk.tildeslash.cookbook.common.datastore.MySQLConnector;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import dk.tildeslash.cookbook.common.recipe.Ingredient;
import dk.tildeslash.cookbook.common.recipe.Recipe;
import dk.tildeslash.cookbook.common.recipe.RecipeSection;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Editor implements ChangeListener{

    private final JFrame frame = new JFrame("Recipe Editor");
    private final JPanel mainPanel;
    JScrollPane scrollPane;
    private final RecipeInformationPanel informationPanel = new RecipeInformationPanel();
    private List<RecipeSectionPanel> sections = new LinkedList<>();
    private boolean isChanged = false;
    private DataStoreConnector db;

    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem newMenuItem = new JMenuItem("New Recipe", KeyEvent.VK_N);
    private JMenuItem editMenuItem = new JMenuItem("Edit Existing Recipe", KeyEvent.VK_E);
    private JMenuItem saveMenuItem = new JMenuItem("Save Recipe", KeyEvent.VK_S);
    private JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
    private JMenu editMenu = new JMenu("Edit");
    private JMenuItem addSectionMenuItem = new JMenuItem("Add Section", KeyEvent.VK_A);
    private JMenuItem removeSectionMenuItem = new JMenuItem("Remove Section", KeyEvent.VK_R);
    private JMenuItem editIngredientMenuItem = new JMenuItem("Edit Base Ingredient", KeyEvent.VK_E);

    private Recipe oldRecipe = null; //used for when we edit an recipe instead of creating a new one

    private static final Logger LOGGER = Logger.getLogger(Editor.class);

    private boolean isStandAloneApplication = false;

    public static void main(String args[]) {
        //TODO if debug: LOGGER.getRootLogger().setLevel((Level) Level.DEBUG);
        if ( args.length > 1 ) {
            System.out.println("The editor takes one argument which is the configuration file.");
            System.out.println("If no configuration file is supplied, or the given configuration file is invalid, the default configuration file (./cookbook.conf) will be used");
        } if ( args.length == 1 ){
            try{
                Configuration.load(new File(args[0]));
            } catch (IOException e){
                System.out.println("Error while reading configuration file '" + args[0] + "'.");
                System.out.println("\t" + e.getMessage());
            }
        } else {
            try{
                Configuration.load(new File("cookbook.conf"));
            } catch (IOException e){
                System.out.println("Error while reading configuration file '" + args[0] + "'.");
                System.out.println("\t" + e.getMessage());
            }
        }
        new Editor().isStandAloneApplication = true;
    }

    public Editor() {
        try{
           db = MySQLConnector.getInstance();
        } catch (NotConfiguredException | DataStoreException e){
            showErrorMessage("An error occurred while connecting to the database,\n" +
                    "please check your configuration file and the connection to the database.",
                    "Database Error");
            System.exit(-2);
        }
        frame.setLayout(new BorderLayout());
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(informationPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        setupMenu();
        setupEnterActionForAllButtons();
        setupListeners();

        RecipeSectionPanel section = new RecipeSectionPanel(1);
        section.addChangeListener(this);
        sections.add(section);
        mainPanel.add(section);

        scrollPane.setPreferredSize(new Dimension(767, 600));
        frame.pack();

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    public Editor(Recipe recipe){
        this();

        loadRecipe(recipe);
    }

    private void setupMenu(){
        menuBar.add(fileMenu);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(newMenuItem);
        fileMenu.add(editMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(exitMenuItem);

        menuBar.add(editMenu);
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(addSectionMenuItem);
        editMenu.add(removeSectionMenuItem);
        editMenu.add(editIngredientMenuItem);

        frame.setJMenuBar(menuBar);
    }

    private void setupEnterActionForAllButtons() {
        InputMap im = (InputMap) UIManager.getDefaults().get("Button.focusInputMap");
        Object pressedAction = im.get(KeyStroke.getKeyStroke("pressed SPACE"));
        Object releasedAction = im.get(KeyStroke.getKeyStroke("released SPACE"));

        im.put(KeyStroke.getKeyStroke("pressed ENTER"), pressedAction);
        im.put(KeyStroke.getKeyStroke("released ENTER"), releasedAction);
    }

    private void setupListeners(){
        informationPanel.addChangeListener(this);
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newClicked();
            }
        });
        editMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editClicked();
            }
        });
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveClicked();
            }
        });
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitClicked();
            }
        });
        addSectionMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSectionClicked();
            }
        });
        removeSectionMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSectionClicked();
            }
        });
        editIngredientMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editIngredientClicked();
            }
        });

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitClicked();
            }
        });

    }

    private void newClicked() {
        if ( ! checkSave()){
            return;
        }

        oldRecipe = null;
        clearRecipe();

        RecipeSectionPanel sectionPanel = new RecipeSectionPanel(1);
        sectionPanel.addChangeListener(this);
        sections.add(sectionPanel);
        mainPanel.add(sectionPanel);
        notChanged();
    }

    private void editClicked() {
        if( ! checkSave()){
            return;
        }

        List<Recipe> recipeList;
        try{
            recipeList =  db.retrieveAllRecipes();
        } catch (DataStoreException e){
            showErrorMessage("An error occurred while connecting to the database,\n" +
                    "please check your configuration file and the connection to the database.",
                    "Database Error");
            return;
        }
        Object[] recipes = recipeList.toArray();

        Object answer = JOptionPane.showInputDialog(frame,
                "Which recipe do you want to edit?",
                "Edit Recipe",
                JOptionPane.PLAIN_MESSAGE,
                null,
                recipes,
                recipes[0]);

        if(answer == null){
            return;
        }

        Recipe recipe = (Recipe)answer;
        loadRecipe(recipe);
    }

    private void loadRecipe(Recipe recipe){
        clearRecipe();
        oldRecipe = recipe;

        informationPanel.setName(recipe.getName());
        informationPanel.setSource(recipe.getSource());
        informationPanel.setTime(recipe.getTime());
        informationPanel.setCalories(recipe.getCalories());
        informationPanel.setPortions(recipe.getPortions());

        List<RecipeSection> recipeSections = recipe.getSections();
        for(int i = 0; i < recipeSections.size(); i++){
            RecipeSectionPanel sectionPanel = new RecipeSectionPanel(recipeSections.get(i), i+1);
            sectionPanel.addChangeListener(this);
            sections.add(sectionPanel);
            mainPanel.add(sectionPanel);
        }

        notChanged();
    }

    private boolean checkSave(){
        if ( isChanged ) {
            return showSaveDialog("The recipe is not yet saved, do you want to save before editing another recipe.\n" +
                    " If you choose not to save, any changes since last save will be lost");
        }
        return true;
    }

    private void saveClicked() {
        save();
    }

    private void exitClicked() {
        if (isChanged) {
            boolean response = showSaveDialog("The recipe is not yet saved, do you want to save before editing another recipe.\n" +
                    " If you choose not to save, any changes since last save will be lost");

            if (!response) {
                return;
            }
        }

        if ( isStandAloneApplication ){
        System.exit(0);
        }

        frame.dispose();
    }

    private void removeSectionClicked() {
        Object[] sectionIDs = new Object[sections.size()];
        for(int i = 1; i <= sectionIDs.length; i++){
            sectionIDs[i-1] = sections.get(i-1).getSectionNumber();
        }

        Object answer = JOptionPane.showInputDialog(frame,
                "Which section do you want to remove?",
                "Remove Section",
                JOptionPane.PLAIN_MESSAGE,
                null,
                sectionIDs,
                sectionIDs[0]);

        if(answer == null){
            return;
        }
        int removedSectionID = (Integer)answer;

        RecipeSectionPanel removedSection = null;
        List<RecipeSectionPanel> newSections = new LinkedList<>();
        for(RecipeSectionPanel panel : sections){
            if(panel.getSectionNumber() == removedSectionID){
                removedSection = panel;
            } else {
                newSections.add(panel);
                panel.setSectionNumber(newSections.indexOf(panel) + 1);
            }
        }

        mainPanel.remove(removedSection);
        sections = newSections;

        mainPanel.updateUI();
        Dimension dim = frame.getSize();
        frame.setPreferredSize(dim);
        frame.pack();

        changed();
    }

    private void editIngredientClicked(){

        List<Ingredient> ingredientList;
        try{
            ingredientList = db.retrieveAllIngredients();
        } catch (DataStoreException e){
            showErrorMessage("There was a problem retrieving the ingredients from the data store.", "Data store error");
            return;
        }

        Collections.sort(ingredientList, new Comparator<Ingredient>() {
            @Override
            public int compare(Ingredient o1, Ingredient o2) {
                return o1.getSingular().compareTo(o2.getSingular());
            }
        });

        Object[] ingredients = ingredientList.toArray();

        Object answer = JOptionPane.showInputDialog(frame,
                        "Which ingredient do you want to edit?",
                        "Edit Ingredient",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        ingredients,
                        ingredients[0]);

        if(answer == null){
            return;
        }

        new CreateIngredientDialog((Ingredient)answer).showDialog(frame);
        frame.requestFocus();
    }

    private void addSectionClicked() {
        RecipeSectionPanel newSection = new RecipeSectionPanel(sections.size() + 1);
        sections.add(newSection);
        mainPanel.add(newSection);
        newSection.addChangeListener(this);
        Dimension dim = frame.getSize();
        frame.setPreferredSize(dim);
        frame.pack();
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());

        changed();
    }

    /**
     * @param msg Question to the user.
     * @return false if the user cancels or there is an error in saving. Returns true if the save is succesfull or the user chooses not to save.
     */
    private boolean showSaveDialog(String msg) {
        int response = JOptionPane.showConfirmDialog(frame,
                msg,
                "Recipe not yet saved",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION) {
            return false;
        } else
            if (response == JOptionPane.YES_OPTION) {  //save
            return save();
        } else { //Do not save
            return true;
        }
    }

    private boolean save() {

        String name = informationPanel.getName().trim();
        String source = informationPanel.getSource().trim();
        int time = informationPanel.getTime();
        int calories = informationPanel.getCalories();
        int portions = informationPanel.getPortions();

        if ( name == null || name.equals("")){
            showErrorMessage("You need to specify a name for the recipe", "Missing Name");
            return false;
        }

        if ( portions < 0 ){
            showErrorMessage("You need to specify the number of portions for the recipe.", "Missing portions");
            return false;
        }

        Recipe newRecipe = assembleRecipe(name, time, calories, source, portions);

        List<Recipe> recipes;

        try{
            recipes = db.retrieveAllRecipes();
        } catch (DataStoreException e){
            showErrorMessage("The database contains corrupted data.\n" +
                    "You cannot continue until this is fixed.\n" +
                    "Error message: " + e.getMessage(), "Fatal Database Error");
            return false;
        }

        if(recipes.contains(newRecipe) && ! newRecipe.equals(oldRecipe)){
            showErrorMessage("There already exists an recipe with this name,\n" +
                    "please choose a new name", "Duplicate Recipe Name");
            return false;
        }
        //First time we save this recipe
        if(oldRecipe == null){
            if(db.addRecipe(newRecipe)){
                notChanged();
                oldRecipe = newRecipe;
               return true;
            } else {
                showErrorMessage("There was an error saving the recipe to the database.", "Database Error");
                return false;
            }
        } else { //This recipe has been saved before and is now being updated
            if(db.updateRecipe(oldRecipe, newRecipe)){
                notChanged();
                oldRecipe = newRecipe;
                return true;
            } else {
                showErrorMessage("There was an error saving the recipe to the database." , "Database Error");
                return false;
            }
        }
    }

    private void showErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private Recipe assembleRecipe(String name, int time, int calories, String source, int portions){
        List<RecipeSection> recipeSections = new LinkedList<>();
        for(RecipeSectionPanel panel: sections){
            recipeSections.add(panel.getRecipeSection());
        }
        return new Recipe(name, time, calories, source, recipeSections, portions);
    }

    private void clearRecipe(){
        informationPanel.setCalories(0);
        informationPanel.setName("");
        informationPanel.setSource("");
        informationPanel.setTime(0);
        informationPanel.setPortions(0);
        mainPanel.removeAll();
        sections.clear();
        mainPanel.updateUI();
    }

    @Override
    public void changed() {
        String title = frame.getTitle();
        if( ! title.endsWith("*") ){
            frame.setTitle(title + "*");
        }
        isChanged = true;
    }

    /**
     * Removes trailing * from the title as to indicate that the
     * recipe is no longer unsaved.
     * Sets field variable isChanged to false.
     */
    private void notChanged() {
        frame.setTitle(frame.getTitle().replaceAll("\\*+$", ""));
        isChanged = false;
    }
}