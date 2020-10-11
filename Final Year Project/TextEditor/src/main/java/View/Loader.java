package View;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import Controller.InputConsole;
import Controller.KeyEvents;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The Launcher for the text editor
 * Contains the stage, primary scene, menu bar, and tab pane
 * @author Sam
 *
 */
public class Loader extends Application{
	public static List<String> config = new ArrayList<String>(); //Stores all config options

	static MenuBar menuBar; //All of the scene contents
	static VBox layout;	//A VBox to specify the layout of scene
	static Scene scene;
	public static Stage stage;
	public static TabPane tabPane;
	public static Thread ConsoleThread;
	
	static int height; //The height and width of the application
	static int width;

	//public static String resourcesDir = "E:\\Resources\\"; //The Directory for all resources

	/**
	 * Called at launch
	 * @param args
	 */
	public static void main(String[] args) {
		getSettings(); //Load the config file

		ConsoleThread = new Thread(new InputConsole()); //Start a new input console thread
		ConsoleThread.start();

		launch(args); //Launch the JavaFX thread
	}

	/**
	 * Loads config.txt into config List
	 */
	public static void getSettings() {
		try {
			Stream <String> lines = Files.lines(Paths.get(Loader.class.getResource("config.txt").toURI())); //Stream the config file
			//Stream <String> lines = Files.lines(Paths.get("E:/Resources/config.txt"));
			lines.forEach(x -> config.add(x)); //Add all lines to config
			lines.close();
			
			System.out.println("file  Loader: Config File Loaded");

		} catch (IOException | URISyntaxException e) {
			System.out.print("ERROR Loader: No file found");
			e.printStackTrace();
		}
	}

	/**
	 * JavaFX Application Start
	 */
	@Override
	public void start(Stage arg0) {
		tabPane = new TabPane();
		height = Integer.valueOf(Loader.config.get(1));
		width = Integer.valueOf(Loader.config.get(4));

		Menu fileMenu = new Menu("File");	//Create all menu items and add them to the menubar
		MenuItem fileMenuNew = new MenuItem("(Ctrl+n)   New");
		fileMenuNew.setOnAction(e -> {
			try {
				KeyEvents.newTab();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		MenuItem fileMenuOpen = new MenuItem("(Ctrl+o)   Open");
		fileMenuOpen.setOnAction(e -> {
			try {
				KeyEvents.open();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		MenuItem fileMenuSave = new MenuItem("(Ctrl+s)   Save");
		fileMenuSave.setOnAction(e -> KeyEvents.save((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		MenuItem fileMenuSaveAs = new MenuItem("               Save As");
		fileMenuSaveAs.setOnAction(e -> KeyEvents.saveAs((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		fileMenu.getItems().addAll(fileMenuNew,fileMenuOpen,fileMenuSave,fileMenuSaveAs);

		Menu editMenu = new Menu("Edit");
		MenuItem editMenuUndo = new MenuItem("(Ctrl+z)   Undo");
		editMenuUndo.setOnAction(e -> KeyEvents.undo((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		MenuItem editMenuRedo = new MenuItem("(Ctrl+y)   Redo");
		editMenuRedo.setOnAction(e -> KeyEvents.redo((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		MenuItem editMenuFind = new MenuItem("(Ctrl+f)   Find");
		editMenuFind.setOnAction(e -> KeyEvents.find((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		MenuItem editMenuReplace = new MenuItem("(Ctrl+h)   Replace");
		editMenuReplace.setOnAction(e -> KeyEvents.findAndReplace((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		editMenu.getItems().addAll(editMenuUndo,editMenuRedo,editMenuFind,editMenuReplace);

		Menu advMenu = new Menu("Advanced");
		MenuItem advMenuMap = new MenuItem("(Ctrl+m)   Map");
		advMenuMap.setOnAction(e -> KeyEvents.map((CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent()));
		advMenu.getItems().addAll(advMenuMap);

		menuBar = new MenuBar();
		menuBar.minWidth(width);
		menuBar.getMenus().addAll(fileMenu,editMenu,advMenu);

		
		Tab firstTab = new Tab("unsaved"); //Create first tab and add it to the tab pane
		try {
			firstTab.setContent(new CodeTextArea());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		tabPane.getTabs().add(firstTab);
		

		tabPane.getSelectionModel().selectedItemProperty().addListener(x -> tabListner()); //add tabListner to the tabpane selection model

		layout = new VBox();	//add menu and tab pane to layout
		layout.getChildren().add(menuBar);
		layout.getChildren().add(tabPane);
		layout.setPrefHeight(height);

		tabPane.prefHeightProperty().bind(layout.heightProperty()); //make tab pane expand with layout
		tabPane.prefWidthProperty().bind(layout.widthProperty());

		scene = new Scene(layout, width, height); //set scene and stage

		stage = arg0;
		stage.setScene(scene);
		
		stage.show();

		Loader.tabPane.getSelectionModel().getSelectedItem().getContent().requestFocus();
	}

	/**
	 * If there are no tabs in tab pane adds a new empty tab
	 * Updates stage title to the file related to the current selected tab
	 */
	public void tabListner() {
		if(tabPane.getTabs().isEmpty()) {	//If tab pane is empty
			Tab firstTab = new Tab("unsaved"); //Create a new tab
			try {
				firstTab.setContent(new CodeTextArea()); //Add a codetextarea
			} catch (Exception e) {
				e.printStackTrace();
			}
			tabPane.getTabs().add(firstTab);	//add the tab to the tab pane
		}

		CodeTextArea console = (CodeTextArea) tabPane.getSelectionModel().getSelectedItem().getContent(); //The code text area of the current tab
		if (console.file.exists()) {						//If the tab has a related file
			stage.setTitle(console.file.getAbsolutePath()); //Set the stage title to that absolute path of the file
			
		} else {				//Else
			stage.setTitle("");	//Clear the stage title
		}
	}
}
