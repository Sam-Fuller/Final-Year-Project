package Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import View.CodeTextArea;
import View.Loader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * A class of many reused key events
 * @author Sam
 *
 */
public class KeyEvents {
	/**
	 * Adds a new tab to loader's tab pane
	 */
	public static void newTab() {
		Tab tab = new Tab("unsaved");	//Creating a new tab

		CodeTextArea caller = new CodeTextArea();	//Creating and setting the contents of the new tab
		tab.setContent(caller);

		Loader.tabPane.getTabs().add(tab);	//Adding the new tab to the tab pane
		Loader.tabPane.getSelectionModel().getSelectedItem().getContent().requestFocus();
		caller.update();
	}

	/**
	 * Prompts the user for a file and opens the file in a new tab
	 */
	public static void open() {
		Tab tab = new Tab("unsaved");	//Creates a new tab for the file
		CodeTextArea caller = new CodeTextArea();
		tab.setContent(caller);

		FileChooser Chooser = new FileChooser();	//Promts the user for a file using a file chooser
		Chooser.setTitle("Open File");
		File selectedFile = Chooser.showOpenDialog(Loader.stage);

		if(selectedFile != null) {	//If the user has selected a file
			caller.file = selectedFile;

			caller.Text.reset();

			loader(caller);	//Call loader to load the selected file

			Loader.tabPane.getTabs().add(tab);	//Add the tab to the tab pane
			Loader.tabPane.getSelectionModel().select(tab);
			caller.update();	//Update the text on the new tab
		}
	}
	
	/**
	 * Loads the callers file in to the caller
	 * @param caller - The code text area for the file to load into
	 */
	public static void loader(CodeTextArea caller) {
		List<String> fileName = Arrays.asList(caller.file.toString().split("\\."));
		String path = fileName.get(fileName.size()-1);	//Get the file extension

		if(Loader.config.get(7).equals("auto")) {	
			if(!new File("src/main/resources/view/" + path + ".css").exists()) {
				System.out.println("file  Console: css does not exist");	//Send error if css file does not exist
			}else if(!new File("src/main/resources/view/" + path + ".txt").exists()){
				System.out.println("file  Console: txt does not exist");	//Send error if txt file does not exist
			}else {
				caller.importStyle(path);	//Loads the files if both exist
			}
		}

		try {
			Stream <String> lines = Files.lines(caller.file.toPath());	//Load the file into the linelist
			lines.forEach(x -> caller.Text.load(x));
			lines.close();
			System.out.println("file  KeyEvents: File Loaded");

		} catch (IOException e) {
			System.out.print("ERROR KeyEvents: No file selected");
			e.printStackTrace();
		}
	}

	/**
	 * Saves the text of a code text area to its file
	 * @param caller - The code text area to be saved to file
	 */
	public static void save(CodeTextArea caller) {
		caller.calcChange();	//Calculate any changes

		if (caller.file.exists()){	//If the file exists
			saveto(caller);	//Call saveto with the caller

			Alert alert = new Alert(AlertType.INFORMATION);	//Alert the user the file has been saved
			alert.setTitle("Saved");
			alert.setHeaderText("File has been saved");
			alert.setContentText(caller.file.getAbsolutePath());

			alert.showAndWait();

		}else {
			saveAs(caller);	//if file does not exist call saveas
		}

	}

	/**
	 * Saves the text of a code text area to its file
	 * @param caller - The code text area to be saved to file
	 */
	public static void saveto(CodeTextArea caller) {
		try {			
			PrintWriter pw = new PrintWriter(new FileOutputStream(caller.file));
			caller.Text.toList().stream().forEach(x -> pw.println(x));	//Saves the text to the file
			pw.close();

			System.out.println("file  KeyEvents: File Saved");
		} catch (IOException e) {
			System.out.print("ERROR KeyEvents: No file selected");
		}
	}

	/**
	 * prompts the user for a file and saves the text to that file
	 * @param caller = the code text area to be saved to file
	 */
	public static void saveAs(CodeTextArea caller) {
		caller.calcChange();	//Calculate any changes

		FileChooser Chooser = new FileChooser();	//Prompt the user for a file
		Chooser.setTitle("Open File");
		caller.file = Chooser.showSaveDialog(Loader.stage);
		caller.update();

		if(caller.file != null) {	//If the user selected a file
			if (caller.file.exists()) {
				try {
					PrintWriter pw = new PrintWriter(new FileOutputStream(caller.file));	//Saves the text to file
					caller.Text.toList().stream().forEach(x -> pw.println(x));
					pw.close();

					System.out.println("file  KeyEvents: File Saved");

					Alert alert = new Alert(AlertType.INFORMATION);	//Alerts the user the file has been saved
					alert.setTitle("Saved");
					alert.setHeaderText("File has been saved");
					alert.setContentText(caller.file.getAbsolutePath());

					alert.showAndWait();

				} catch (IOException e) {
					System.out.print("ERROR KeyEvents: No file selected");
					e.printStackTrace();
				}
			}
		}else {
			caller.file = new File("");
		}
	}

	/**
	 * Undoes the last change
	 * @param caller - The code text area for the last change to be undone
	 */
	public static void undo(CodeTextArea caller) {
		caller.Text.undo();	//Undo the last change
		caller.update();	//Update the code text area
	}

	/**
	 * Redoes the last undo
	 * @param caller - The code text area for the last change to be redone
	 */
	public static void redo(CodeTextArea caller){
		caller.Text.redo();	//Redo the last undo
		caller.update();	//Update the code text area
	}

	/**
	 * Prompts the user for a string, then highlights all instances of the string in the text
	 * @param caller - The code text area to highlight
	 */
	public static void find(CodeTextArea caller) {
		caller.calcChange();	//Calculate any changes

		TextInputDialog dialog = new TextInputDialog(caller.findValue);	//Prompt the user for a string to find
		dialog.setTitle("Find");
		dialog.setHeaderText(caller.file.getAbsolutePath());
		dialog.setContentText("Find:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){	//If the user gives a string
			caller.findValue = result.get();	//Set the find value to the given string
		}
		caller.update();	//Update the code text area
	}

	/**
	 * Prompts the user for two strings, highlights all instances of one and replaces it with the other
	 * @param caller - The code text area of text to be searched and matches replaced
	 */
	public static void findAndReplace(CodeTextArea caller) {
		caller.calcChange();	//Calculate any changes

		Dialog<Pair<String, String>> dialog = new Dialog<>();	//Create a dialog with two textboxes
		dialog.setTitle("Find and Replace");
		dialog.setHeaderText(caller.file.getAbsolutePath());

		ButtonType submitButtonType = new ButtonType("Replace", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField find = new TextField();
		find.setPromptText("Find");
		TextField replace = new TextField();
		replace.setPromptText("Replace with");

		grid.add(new Label("Find:"), 0, 0);
		grid.add(find, 1, 0);
		grid.add(new Label("Replace with:"), 0, 1);
		grid.add(replace, 1, 1);

		Node loginButton = dialog.getDialogPane().lookupButton(submitButtonType);
		loginButton.setDisable(true);

		find.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == submitButtonType) {
				return new Pair<>(find.getText(), replace.getText());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();

		if(result.isPresent()) {	//If the user has given two strings
			caller.replaceText(caller.getText().replaceAll(result.get().getKey(), result.get().getValue()));	//replace all instances of the first string with the second
		}

		caller.calcChange();	//Update the code text area
		caller.update();
	}

	/**
	 * Prompts the user for a lambda and applies it to a code text area
	 * @param caller - the code text area for the function to be applied to
	 */
	public static void map(CodeTextArea caller) {
		caller.calcChange();	//Calculate any changes

		TextInputDialog dialog = new TextInputDialog();	//Prompt the user for an input
		dialog.setTitle("Apply Lambda Function");
		dialog.setHeaderText(caller.file.getAbsolutePath());
		dialog.setContentText("x -> ");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){	//If the user has inputted a string
			String stringFunction = result.get();

			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
			try {
				@SuppressWarnings("unchecked")
				Function<String,String> function = (Function<String,String>)engine.eval(	//Change the String input into a function
						String.format("new java.util.function.Function(%s)", "function(x) " + stringFunction));
				try {
					caller.Text.map(function);	//Apply the function to the code text area
				} catch (Exception e) {
					System.out.println("ERROR Invalid lambda function");
				}
			} catch (ScriptException e) {
				System.out.println("ERROR Invalid lambda function");	//Throw an error if an invalid function is entered
			}
		}
		caller.update();	//Update the code text area
	}
}
