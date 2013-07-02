package dk.tildeslash.cookbook.viewer;

import dk.tildeslash.cookbook.common.datastore.Configuration;
import dk.tildeslash.cookbook.viewer.SelectionPanel.SelectionPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 20/06/12
 * Time: 13:49
 */
public class Viewer{

    private final JFrame frame = new JFrame("Recipe Viewer");
    private final JTabbedPane mainPane = new JTabbedPane();

    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem newMenuItem = new JMenuItem("New Recipe");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");

    private static final Logger LOGGER = Logger.getLogger(Viewer.class);

    private Map<String, Class<SelectionPanel>> selectionPanels = new LinkedHashMap<>(3);

    public static void main(String args[]) {
        //TODO if debug: LOGGER.getRootLogger().setLevel((Level) Level.DEBUG);
        if ( args.length > 1 ) {
            System.out.println("The viewer takes one argument which is the configuration file.");
            System.out.println("If no configuration file is supplied, or the given configuration file is invalid, the default configuration file (./cookbook.conf) will be used");
        } if ( args.length == 1 ){
            try{
                Configuration.load(new File(args[0]));
            } catch (IOException e){
                LOGGER.error("Error while reading configuration file (" + args[0] + "), " + e.getMessage());
                System.out.println("Error while reading configuration file '" + args[0] + "'.");
                System.out.println("\t" + e.getMessage());
            }
        }
        new Viewer();
    }

    public Viewer(){

        setupComponents();
        setupMenu();
        setupListeners();
        loadSelectionPanels();

        mainPane.setPreferredSize(new Dimension(645, 800));
        frame.pack();

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    private void setupComponents(){
        frame.setLayout(new BorderLayout());
        frame.add(mainPane, BorderLayout.CENTER);
    }

    private void setupMenu(){
        menuBar.add(fileMenu);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(newMenuItem);
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        fileMenu.addSeparator();
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        fileMenu.add(exitMenuItem);

        frame.setJMenuBar(menuBar);
    }

    private void setupListeners(){
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newClicked();
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitClicked();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitClicked();
            }
        });
     }

    private void loadSelectionPanels(){
        //Could end up loading this using reflection to enable easy plugins
        String[] panelClassNames = {"SearchSelectionPanel",
                                    "RandomSelectionPanel",
                                    "IngredientMatchingSelectionPanel"};

        String pkg = "dk.tildeslash.cookbook.viewer.SelectionPanel";
        for ( String className : panelClassNames ){
            try {
                @SuppressWarnings("unchecked")
                Class<SelectionPanel> clazz = (Class<SelectionPanel>) Class.forName(pkg + '.' + className);
                String title = clazz.getConstructor().newInstance().getTitle();
                selectionPanels.put(title, clazz);
            } catch (ClassNotFoundException e) {
                String msg = "Unable to find class for selection panel " + className;
                showErrorMessage(msg, "Unable to find class");
                LOGGER.error(msg, e);
            } catch (ClassCastException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e){
                String msg = "Unable to load selection panel of type " + className;
                showErrorMessage(msg, "Unable to load selection panel");
                LOGGER.error(msg, e);
            }

        }
    }

    private void newClicked(){
        Object[] options = selectionPanels.keySet().toArray();

        String choice = (String)JOptionPane.showInputDialog(frame,
                                                            "How do you want to find the new recipe?",
                                                            "Choose selection model",
                                                            JOptionPane.QUESTION_MESSAGE,
                                                            null,
                                                            options,
                                                            "Select recipe by name");
        if ( choice == null ) {
            return;
        }

        try {
            Class<SelectionPanel> selectionPanelClass = selectionPanels.get(choice);
            SelectionPanel selectionPanel = selectionPanelClass.getConstructor().newInstance();
            addTab(selectionPanel, choice);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |NoSuchMethodException e) {
            String msg = "Unable to load selection panel of type " + choice;
            showErrorMessage(msg, "Unable to display selected panel");
            LOGGER.error(msg, e);
        }
    }

    private void addTab(SelectionPanel panel, String title){
        final TabComponentWithClose tabComponent = new TabComponentWithClose(mainPane, title);
        mainPane.addTab(null, new TabPanel(panel, tabComponent));
        mainPane.setTabComponentAt(mainPane.getTabCount() - 1, tabComponent);
        mainPane.setSelectedIndex(mainPane.getTabCount() - 1);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new ViewerKeyboardDispatcher(mainPane));

        panel.takeFocus();
    }

    private void exitClicked() {
        System.exit(0);
    }

    private void showErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
