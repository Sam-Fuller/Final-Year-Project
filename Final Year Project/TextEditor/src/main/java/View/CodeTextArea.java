package View;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import Controller.Listner;
import Lib.LineList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * An extension on CodeArea to involve the text editor features
 * @author Sam
 *
 */
public class CodeTextArea extends CodeArea{

	public String findValue = "";	//The find string to be highlited
	public LineList Text = new LineList();	//The linelist where all the text is stored
	public File file = new File("");	//The file the relevant to the linelist

	List<String> patterns = new ArrayList<String>();	//The RegEx patterns needed for syntax highlighting

	/**
	 * Default constructor
	 */
	public CodeTextArea(){
		this.setParagraphGraphicFactory(LineNumberFactory.get(this));

		String path = Loader.config.get(7);
		if(!path.equals("auto")) {	//if the path is set to auto
			if(!new File("src/main/resources/view/" + path + ".css").exists()) {	//attempt to find css and text files
				System.out.println("file  Console: css does not exist");
			}else if(!new File("src/main/resources/view/" +path + ".txt").exists()){
				System.out.println("file  Console: txt does not exist");
			}else {
				this.importStyle(path);	//if both are present load them both
			}
		}
		
		this.getStylesheets().add(CodeTextArea.class.getResource("default.css").toExternalForm());
		//this.getStylesheets().add("file:///E:/Resources/default.css");

		@SuppressWarnings("unused")
		Subscription syntaxHighlighterThread = this.multiPlainChanges()	//Create a subscription that highlights the text 500ms after the last change
		.successionEnds(Duration.ofMillis(500))		
		.subscribe(x -> syntaxHighlighter());

		this.setOnMouseClicked(e -> Listner.mouseClicked(this));		//bind Listner events
		this.setOnKeyPressed(e -> Listner.keyPressed(this, e.getCode()));
		this.setOnKeyReleased(e -> Listner.keyReleased(this, e.getCode()));
		this.setWrapText(false);

		HBox.setHgrow(this, Priority.ALWAYS);
	}

	/**
	 * Imports the style sheet and patterns of a given language
	 * @param lang - The language of the css/txt files
	 */
	public void importStyle(String lang){
		this.getStylesheets().add(CodeTextArea.class.getResource(lang + "highlighter.css").toExternalForm());	//adds the css file to this
		//this.getStylesheets().add("file:///E:/Resources/" + lang + ".css");
		System.out.println("file  Console: " + lang + ".css" + " Loaded");


		List<String> style = new ArrayList<String>();
		try {
			Stream <String> lines = Files.lines(Paths.get(CodeTextArea.class.getResource(lang + "highlighter.txt").toURI()));	//Reading style from the txt file
			//Stream <String> lines = Files.lines(Paths.get("E:/Resources/" + lang + ".txt"));
			
			lines.forEach(x -> style.add(x));
			lines.close();
			System.out.println("file  Console: " + lang + ".txt" + " Loaded");

		} catch (IOException | URISyntaxException e) {
			System.out.print("ERROR Console: No class file found");
			e.printStackTrace();
		}

		for(int i = 0; i < style.size(); i++) {	//change the raw strings into groups and their regexs
			String current = style.get(i);
			if(current.trim() != "") {
				if(current.trim().endsWith("{")) {
					current = current.substring(0, current.length()-1);
					current = current.trim();

					patterns.add(current);
					patterns.add("");
				} else if(!current.trim().startsWith("}")) {
					patterns.set(patterns.size() - 1, patterns.get(patterns.size() - 1) + current.trim());
				}
			}
		}
		
		
	}

	/**
	 * 
	 * @return
	 */
	private void syntaxHighlighter() {
		this.calcChange();
		String patternTotal = "";

		if(!findValue.equals("")) {
			patternTotal+="(?<";	//add the find group to the pattern
			patternTotal+="FIND";
			patternTotal+=">";
			patternTotal+=findValue;
			patternTotal+=")|";
		}

		for(int i = 0; i< patterns.size(); i+=2) {	//Concatanate all syntax highlighter regexes and their groups into one pattern
			patternTotal+="(?<";
			patternTotal+=patterns.get(i).toUpperCase();
			patternTotal+=">";
			patternTotal+=patterns.get(i+1);
			patternTotal+=")|";
		}

		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

		if(patternTotal.length() > 0) {
			patternTotal = patternTotal.substring(0, patternTotal.length()-1);

			Pattern pattern = Pattern.compile(patternTotal);	//compile the pattern
			Matcher matcher = pattern.matcher(this.getText());	//create a matcher with the pattern and the text
			int last = 0;

			while(matcher.find()) {	//changes the pattern groups to css tags
				String styleClass = "";
				if(!findValue.equals("")) {
					styleClass = matcher.group("FIND") != null ? "find" : "";
				}
				if(styleClass.equals("")) {
					for(int i = 0; (i < patterns.size()) && (styleClass.equals("")); i+=2) {
						styleClass = matcher.group(patterns.get(i).toUpperCase()) != null ? patterns.get(i) : "";
					}
				}

				spansBuilder.add(Collections.emptyList(), matcher.start() - last);
				spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());	//generating the spansbuilder from the matcher
				last = matcher.end();
			}
			spansBuilder.add(Collections.emptyList(), this.getText().length() - last);
			this.setStyleSpans(0, spansBuilder.create());	//returning the generated spans builder
		}
	}

	/**
	 * returns the current caret position
	 * @return - the current caret position
	 */
	public int getCaretPos() {
		return this.getCaretPosition();
	}

	/**
	 * Sets the text of the code text area
	 * @param text - A list of lines to be displayed
	 */
	public void setText(List<String> text){
		text = text.stream()	//Append a newline character
				.map(x -> x + "\n")
				.collect(Collectors.toList());

		String strText = text.stream().reduce("", (x, y) -> x + y); //collect the list into a single string
		strText = strText.substring(0, strText.length() - 1);	//remove the last newline character

		this.clear();	//remove the current text

		this.replaceText(strText);	//add the new text
	}

	/**
	 * Get the text in an ArrayList<String>
	 * @return - The text displayed
	 */
	public List<String> getListText() {
		return seperate(this.getText());
	}

	/**
	 * Calculates the changes between the text in the linelist and that displayed
	 */
	@SuppressWarnings("unused")
	public void calcChange(){
		List<String> currentText = seperate(this.getText());	//the text displayed
		List<String> expectedText = this.Text.toList();	//the text in the linelist

		int line = 0;


		boolean diff = false;
		if(expectedText.size() > 0){
			for(int i = 0; (diff == false)&&(expectedText.size() > 0)&&(currentText.size() > 0); i++) {	//Repeat until the end or a difference is found
				if(expectedText.get(0).equals(currentText.get(0))) {	//if expected and current are equal
					expectedText.remove(0);	//remove the duplicate lines
					currentText.remove(0);
					line++;	//add 1 to the number of lines removed
				} 

				else {
					diff = true;	//stop loop if a difference is found
				}
			}

			diff = false;

			for(int i = 0; (diff == false)&&(expectedText.size() > 0)&&(currentText.size() > 0); i++) {	//Repeat until the start or a difference is found
				if(expectedText.get(expectedText.size()-1).equals(currentText.get(currentText.size()-1))) {	//if expected and current are equal
					expectedText.remove(expectedText.size()-1);	//remove the duplicate lines
					currentText.remove(currentText.size()-1);
				} 

				else{
					diff = true;	//stop loop if a difference is found
				}
			}

			//With all identical lines removed, the two lists contain only the differences

			if ((expectedText.size() > 0) || (currentText.size() > 0)){	//if there are differences
				this.Text.prep();	//create a new undo point
				System.out.println("in    Console: new undo/redo point created");
			}

			if (expectedText.size() > 0){	//remove all lines that appear in expected but not in current
				for(int i = 0; i < expectedText.size(); i++) {
					this.Text.remove(line);
				}
			}

			if (currentText.size() > 0){	//add all lines that appear in current but not in expected
				for(int i = 0; i < currentText.size(); i++) {
					this.Text.add(line+i, currentText.get(i));
				}
			}
		}
	}

	/**
	 * Sepeartes a string into a list by the appearance of a newline character
	 * @param text - The string to be seperated
	 * @return - The seperated list
	 */
	public List<String> seperate(String text){
		List<String> out = new ArrayList<String>();

		int last = 0;
		for (int i = 1; i < text.length(); i++){
			if(text.charAt(i) == '\n') {	//if the character is a newline character
				out.add(text.substring(last, i));	//create a new index in the line containing all characters from the last appearance to this one
				last = i+1;
			}
		}
		out.add(text.substring(last, text.length()));

		return out;
	}

	/**
	 * Updates the view text to that of the linelist text
	 */
	public void update() {
		this.setText(Text.toList());	//Set the text to the same as the linelist

		if(this.file != null) {
			if(this.file.exists()) {	//If this codetextarea has a file associated with it
				Loader.tabPane.getSelectionModel().getSelectedItem().setText(file.getName());	//change the title to the same as the file
			}


			CodeTextArea console = (CodeTextArea) Loader.tabPane.getSelectionModel().getSelectedItem().getContent(); //The code text area of the current tab
			if (console.file.exists()) {						//If the tab has a related file
				Loader.stage.setTitle(console.file.getAbsolutePath()); //Set the stage title to that absolute path of the file

			} else {				//Else
				Loader.stage.setTitle("");	//Clear the stage title
			}
		}

		if(!patterns.isEmpty()) {	//if there is a pattern
			syntaxHighlighter();	//recalculate the syntax highlighting
		}
	}

	/**
	 * returns the abosolute file path as a string
	 * @return - the file path
	 */
	@Override
	public String toString() {
		return file.getAbsolutePath();
	}
}
