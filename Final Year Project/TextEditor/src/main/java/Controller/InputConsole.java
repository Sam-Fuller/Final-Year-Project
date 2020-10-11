package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import View.CodeTextArea;
import View.Loader;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A runnable console input handler
 * @author Sam
 *
 */
public class InputConsole implements Runnable{	
	volatile File selectedfile;
	volatile boolean response;

	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));	//Create new buffer for the console input
		while(true) {	//Repeat while the program is running
			String s;
			try {
				s = br.readLine();	//Wait for and read the next line

				final CodeTextArea change = (CodeTextArea) Loader.tabPane.getSelectionModel().getSelectedItem().getContent();
				change.calcChange();	//Update the target CodeTextArea

				parser(s);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void parser(String s) {
		if(!s.trim().isEmpty()) {
			String split = Character.toString(s.charAt(s.length()-1));	
			List<String> args = new ArrayList<String>(Arrays.asList(s.split(split)));	//Break arguments by last character
			args = args.stream().map(x -> x.trim()).collect(Collectors.toList());
			int repeatnum = -1;
			
			for(int i = args.size()-1; i >= 0; i--) {	//From right to left while there are still commands
				final CodeTextArea target = (CodeTextArea) Loader.tabPane.getSelectionModel().getSelectedItem().getContent();

				switch (args.get(i)) {	//Switch around the current argument

				/**
				 * !
				 * <- T
				 * <- x
				 * removes one argument
				 */
				case "!":
					if(args.size() > i+1) {
						args.remove(i);
						args.remove(i);
					} else {
						System.out.println("no removable arguments");
					}
					break;

					/**
					 * echo
					 * T <- T
					 * x <- x
					 * prints x to the console
					 */
				case "echo":
					//String <- String
					String nextecho;
					if(args.size() > i+1) {
						nextecho = args.get(i+1);
						System.out.println(nextecho);
						args.set(i, nextecho);
					} else {
						System.out.println("no echo arguement");
					}
					args.remove(i);
					break;

					/**
					 * newtab
					 * int <- int
					 * x <- x
					 * creates x number of new tabs
					 * 
					 * newtab
					 * int <-
					 * 1 <-
					 * creates 1 new tab
					 */
				case "newtab":
					//int <- ?int
					int repeatnewtab = 1;

					if(args.size() > i+1) {
						try {
							repeatnewtab = Integer.parseInt(args.get(i+1));
							args.remove(i+1);
							if(repeatnewtab < 1) {
								System.out.println("newtab value can not be less than 1");
								repeatnewtab = 0;
							}
						} catch (NumberFormatException e) {
							System.out.println("invalid newtab arguement: " + args.get(i+1));
						}
					}


					final int repeattab = repeatnewtab;
					Platform.runLater(new Runnable(){
						@Override
						public void run() {
							for (int j = 0; j < repeattab; j++) {
								KeyEvents.newTab();
								Loader.tabPane.getSelectionModel().select(Loader.tabPane.getTabs().size() - 1);
							}
						}
					});	

					args.set(i, Integer.toString(repeatnewtab));
					break;

					/**
					 * activetab
					 * int <- int
					 * index <- index
					 * sets the active tab to index
					 * 
					 * activetab
					 * int <- 
					 * index <-
					 * returns the index of the current active tab
					 */
				case "activetab":
					//int <- ?int
					if(args.size() > i+1) {
						final int tab;
						try {
							tab = Integer.parseInt(args.get(i+1));
							if(tab < 0) {
								System.out.println("tab value can not be less than 0");
							}else {

								Platform.runLater(new Runnable(){
									@Override
									public void run() {
										Loader.tabPane.getSelectionModel().select(tab);
									}
								});	
							}
							args.remove(i);
						} catch (NumberFormatException e) {
							System.out.println("invalid activetab arguement");
							args.set(i, Integer.toString(0));
						}


					} else {
						args.set(i, Integer.toString(Loader.tabPane.getSelectionModel().getSelectedIndex()));
					}
					break;

					/**
					 * getactivetab
					 * int <-
					 * index <-
					 * returns the index of the current active tab
					 */
				case "getactivetab":
					//int <- void
					args.set(i, Integer.toString(Loader.tabPane.getSelectionModel().getSelectedIndex()));
					break;

					/**
					 * open
					 * AbsolueFilePath <- AbsolueFilePath
					 * filepath <- filepath
					 * oepns the file at filepath
					 */
				case "open":
					//AbsolueFilePath <- AbsolueFilePath
					if(args.size() > i+1) {
						final String filename = args.get(i+1);

						Platform.runLater(new Runnable(){						
							@Override
							public void run() {
								Tab tab = new Tab("unsaved");
								CodeTextArea caller;

								caller = new CodeTextArea();
								tab.setContent(caller);

								caller.file = new File(filename);

								if(caller.file.exists()) {
									caller.Text.reset();

									KeyEvents.loader(caller);

									Loader.tabPane.getTabs().add(tab);
									Loader.tabPane.getSelectionModel().select(tab);
									caller.update();

								} else {
									System.out.println("file does not exist");
								}
							}
						});
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						args.remove(i);
					} else {
						System.out.println("not enough open arguments");
					}
					break;

					/**
					 * save
					 * <-
					 * <-
					 * saves the current tab at the file it opened
					 */
				case "save":
					//void <- void
					KeyEvents.saveto(target);
					args.remove(i);

					break;

					/**
					 * undo
					 * int <- int
					 * x <- x
					 * undoes the last x number of actions
					 * 
					 * undo
					 * int <-
					 * 1 <-
					 * undoes the last action, returns 1
					 */
				case "undo":
					//int <- ?int
					int repeatundo;

					if(args.size() > i+1) {
						try {
							repeatundo = Integer.parseInt(args.get(i+1));
							if(repeatundo < 1) {
								System.out.println("undo value can not be less than 1");
							}
						}catch (NumberFormatException e) {
							System.out.println("invalid undo arguement: " + args.get(i+1));		
							repeatundo = 0;
						}
					} else {
						repeatundo = 1;
					}

					final int repeatundofin = repeatundo;

					Platform.runLater(new Runnable(){						
						@Override
						public void run() {
							for (int j = 0; j < repeatundofin; j++) {
								KeyEvents.undo(target);
								target.update();
							}
						}
					});

					args.remove(i);
					break;

					/**
					 * redo
					 * int <- int
					 * x <- x
					 * redoes the last x number of actions
					 * 
					 * redo
					 * int <-
					 * 1 <-
					 * redoes the last action, returns 1
					 */
				case "redo":
					//int <- ?int
					int repeatredo;

					if(args.size() > i+1) {
						try {
							repeatredo = Integer.parseInt(args.get(i+1));
							if(repeatredo < 1) {
								System.out.println("redo value can not be less than 1");
							}
						}catch (NumberFormatException e) {
							System.out.println("invalid redo arguement: " + args.get(i+1));		
							repeatredo = 0;
						}
					} else {
						repeatredo = 1;
					}
					final int repeatredofin = repeatredo;

					Platform.runLater(new Runnable(){						
						@Override
						public void run() {
							for (int j = 0; j < repeatredofin; j++) {
								KeyEvents.redo(target);
								target.update();
							}
						}
					});	
					args.remove(i);
					break;

					/**
					 * find
					 * String <- String
					 * find <- find
					 * sets the find highlight string to find
					 * 
					 * find
					 * <-
					 * <-
					 * empties the find highlight string
					 */
				case "find":
					//?String <- ?String
					if(args.size() > i+1) {
						target.findValue = args.get(i+1);
					}else {
						target.findValue = "";
					}
					args.remove(i);
					break;

					/**
					 * replace
					 * String String <- String String
					 * find replace <- find replace
					 * replaces all instances of find with replace
					 * 
					 * replace
					 * String <- String
					 * remove <- remove
					 * removes all instances of x
					 */
				case "replace":
					//String ?String <- String ?String
					if(args.size() > i+2) {
						final String find = args.get(i+1);
						final String replace = args.get(i+2);

						Platform.runLater(new Runnable(){						
							@Override	
							public void run() {
								target.replaceText(target.getText().replaceAll(find, replace));
								target.calcChange();
								target.update();
							}
						});	
					}else if(args.size() > i+1) {
						final String find = args.get(i+1);
						final String replace = "";

						Platform.runLater(new Runnable(){						
							@Override	
							public void run() {
								target.replaceText(target.getText().replaceAll(find, replace));
								target.calcChange();
								target.update();
							}
						});	
					} else {
						System.out.println("not enough replace arguements");
					}
					args.remove(i);
					break;

					/**
					 * map
					 * Function<String, String> <- Function<String, String>
					 * x <- x
					 * applies x to all lines (example use: map; x + "text")
					 */
				case "map":
					//Function<String, String> <- Function<String, String>
					if (args.size() > i+1) {


						String stringFunction = args.get(i+1); //string function contains part of the function to be evaluated: of the function "x -> x + 1" string function only contains "x + 1"

						ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
						try {
							//Changes the function into the format the engine expects, then evaluates it to produce the function. throws ScriptException
							Function<String, String> function = (Function<String,String>)engine.eval(String.format("new java.util.function.Function(function(x) %s)", stringFunction));
							try {
								//Maps the function onto the text
								target.Text.map(function);
							} catch (Exception e) { //mapping can throw unknown variable exceptions
								System.out.println("Arg failed to resolve: " + args.get(i));
							}

							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									target.update();
								}
							});	
						} catch (ScriptException e) {
							System.out.println("invalid function");
						}
					}else {

						System.out.println("not enough map arguements");
					}
					break;

					/**
					 * add
					 * int String <- int String
					 * linenumber content <- linenumber content
					 * adds a new line with containing content at linenumber
					 * 
					 * add
					 * int <- int
					 * linenumber <- linenumber
					 * adds an empty line at linenumber
					 */
				case "add":
					//int String <- int String
					if(args.size() > i+2) {
						try {
							final int index = Integer.parseInt(args.get(i+1))-1;
							final String text = args.get(i+2);
							if (index > 0) {
								Platform.runLater(new Runnable(){						
									@Override	
									public void run() {
										target.Text.prep();
										target.Text.add(index, text);
										target.update();
									}
								});	
							} else {
								System.out.println("first arguement must be above 0");
							}
						} catch(NumberFormatException e) {
							System.out.println("first arguement must be an int");
						}
					}else if(args.size() > i+1) {
						try {
							final int index = Integer.parseInt(args.get(i+1))-1;
							final String text = "";

							if (index > 0) {
								Platform.runLater(new Runnable(){						
									@Override	
									public void run() {
										target.Text.prep();
										target.Text.add(index, text);
										target.update();
									}
								});	
							} else {
								System.out.println("first arguement must be above 0");
							}
						} catch(NumberFormatException e) {
							System.out.println("first arguement must be an int");
						}
					}else {
						System.out.println("not enough add arguements");
					}
					args.remove(i);
					break;

					/**
					 * linegen
					 * int int String <- int int String
					 * repeat offset text <- repeat offset text
					 * adds repeat number of new lines starting at line number offset containing text
					 * 
					 * linegen
					 * int int <- int int
					 * repeat offset <- repeat offset
					 * adds repeat number of empty new lines starting at line number offset
					 */
				case "linegen":
					//int int String <- int int String
					if (args.size() > i+3) {
						try {
							int repeatgen = Integer.parseInt(args.get(i+1));
							int offset = Integer.parseInt(args.get(i+2))-1;
							String text = args.get(i+3);

							if(repeatgen < 1) {
								System.out.println("first arguement must above 1");
							} else if (offset < 0) {
								System.out.println("second arguement must above 0");
							}else {
								Platform.runLater(new Runnable(){
									@Override
									public void run() {

										CodeTextArea target = (CodeTextArea) Loader.tabPane.getSelectionModel().getSelectedItem().getContent();

										target.Text.prep();
										for (int j = 0; j < repeatgen; j++) {
											target.Text.add(offset, text);
										}

										target.update();

									}
								});	
							}
						} catch(NumberFormatException e) {
							System.out.println("first and second arguements must be ints");
						}
					}else if (args.size() > i+2) {
						try {
							int repeatgen = Integer.parseInt(args.get(i+1));
							int offset = Integer.parseInt(args.get(i+2))-1;
							String text = "";

							if(repeatgen < 1) {
								System.out.println("first arguement must above 1");
							} else if (offset < 0) {
								System.out.println("second arguement must above 0");
							}else {
								Platform.runLater(new Runnable(){
									@Override
									public void run() {

										CodeTextArea target = (CodeTextArea) Loader.tabPane.getSelectionModel().getSelectedItem().getContent();

										target.Text.prep();
										for (int j = 0; j < repeatgen; j++) {
											target.Text.add(offset, text);
										}

										target.update();

									}
								});	
							}
						} catch(NumberFormatException e) {
							System.out.println("first and second arguements must be ints");
						}
					}else {
						System.out.println("not enough linegen arguements");
					}
					args.remove(i);
					break;

					/**
					 * getline
					 * String <- int
					 * line <- linenumber
					 * returns line at position linenumber
					 */
				case "getline":
					//String <- int
					int linenum = 1;

					if(args.size() > i+1) {
						try {
							linenum = Integer.parseInt(args.get(i+1));
							args.remove(i+1);
							if(linenum >= 1){
								args.set(i, target.Text.getLine(linenum-1));
							}else {
								System.out.println("getline value can not be less than 1");
							}
						} catch (NumberFormatException e) {
							System.out.println("invalid getline arguement: " + args.get(i+1));
						}
					}

					break;

					/**
					 * remove
					 * int[] <- int[]
					 * linenumbers <- linenumbers
					 * removes lines at given linenumbers (example use: remove; 1; 6; 70)
					 */
				case "remove":
					//int[] <- int[]
					List<Integer> argsremove = new ArrayList<Integer>();

					if(args.size() > i+1) {
						target.Text.prep();
						for(int j = i+1; j < args.size(); j++) {
							try {
								int argremove = Integer.parseInt(args.get(i+j));
								argsremove.add(argremove-1);									
							} catch (NumberFormatException e) {
								j = args.size();
							}
						}

						Collections.sort(argsremove,Collections.reverseOrder());
						argsremove.stream().forEachOrdered(x -> target.Text.remove(x));

						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								target.update();
							}
						});	
					}
					args.remove(i);
					break;

					/**
					 * edit
					 * int String <- int String
					 * linenumber text <- linenumber text
					 * replaces the line at linenumber with text
					 */
				case "edit":
					//int String <- int String
					if(args.size() > i+2) {

						try {
							final int index = Integer.parseInt(args.get(i+1))-1;
							final String text = args.get(i+2);

							if (index > 0) {
								Platform.runLater(new Runnable(){						
									@Override	
									public void run() {
										target.Text.prep();
										target.Text.edit(index, text);
										target.update();
									}
								});	
							} else {
								System.out.println("first arguement must be above 0");
							}

						} catch(NumberFormatException e) {
							System.out.println("first arguement must be an int");
						}
					}else {
						System.out.println("not enough add arguements");
					}
					args.remove(i);
					break;

					/**
					 * repeat
					 * int <- int
					 * x <- x
					 * repeats the tailing code x times
					 */
				case "repeat":
					if(args.size() > i+1) {
						try {
							int repeatcurrentnum = Integer.parseInt(args.get(i+1));

							if(repeatcurrentnum < 1) {
								System.out.println("repeat argument must be above 1");
								break;
							}

							if(repeatnum < 0) {
								repeatnum = repeatcurrentnum-1;
								args = new ArrayList<String>(Arrays.asList(s.split(split)));
								args = args.stream().map(x -> x.trim()).collect(Collectors.toList());
								i = args.size();
							}else if (repeatnum > 0) {
								args = new ArrayList<String>(Arrays.asList(s.split(split)));
								args = args.stream().map(x -> x.trim()).collect(Collectors.toList());
								i = args.size();
							}else {
								args.remove(i);
							}
							repeatnum--;

						}catch(NumberFormatException e) {
							System.out.println("repeat argument must be an int");
						}
					}else {
						System.out.println("not enough repeat arguements");
					}
					break;

					/**
					 * filedialog
					 * AbsolueFilePath <-
					 * filepath <-
					 * returns the file path selected in a popup file dialog
					 */
				case "filedialog":

					FileChooser Chooser = new FileChooser();
					Chooser.setTitle("Select File");
					response = false;
					selectedfile = null;
					args.remove(i);

					Platform.runLater(new Runnable(){						
						@Override	
						public void run() {
							selectedfile = Chooser.showOpenDialog(Loader.stage);
							response = true;
						}
					});

					while(!response) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if(selectedfile != null) {
						args.add(i, selectedfile.toString());
					} else {
						System.out.println("no file selected");
						args.add(i, "");
					}

					break;

					/**
					 * help
					 * <-
					 * <-
					 * displays information about every command
					 */
				case "help":
					Stream<String> lines;
					try {
						lines = Files.lines(Paths.get(InputConsole.class.getResource("Commands.txt").toURI()));
						lines.forEach(x -> System.out.println(x));
						lines.close();
					} catch (URISyntaxException | IOException e) {
						System.out.println("Commands file not found");
					}

					break;

					/**
					 * converts input arguments into strings
					 */
				default:

					String stringFunction = args.get(i);

					if (i > 0) {
						if (args.get(i-1).equals("map")) {
							break;
						}
					}

					ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
					try {
						Function<Object, Object> function = (Function<Object,Object>)engine.eval(String.format("new java.util.function.Function(%s)", "function(x) " + stringFunction));


						if(args.size() > i+1) {
							try {
								int in = Integer.parseInt(args.get(i+1));
								String out[] = function.apply(in).toString().split("\\.");
								args.set(i,out[0]);
							} catch (NumberFormatException e) {
								args.set(i,function.apply(args.get(i+1)).toString());
							}
						} else {
							try {
								args.set(i,function.apply("").toString());
							} catch (Exception e) {
								System.out.println("Arg failed to resolve: " + args.get(i));
							}
						}

					} catch (ScriptException e) {
						System.out.println("Arg failed to resolve: " + args.get(i));
					}


					break;

				}
			}
		}


	}
}