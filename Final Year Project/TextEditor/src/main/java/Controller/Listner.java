package Controller;

import View.CodeTextArea;
import View.Loader;
import javafx.scene.input.KeyCode;

/**
 * A class containing three listeners
 * Keeps track of what keys are pressed and if a key combo is pressed at the same time runs the relevant keyevent
 * @author Sam
 *
 */
public class Listner {
	//The KeyCodes of all relevant keys and booleans relating to if that key is pressed
	static KeyCode ctrl = KeyCode.CONTROL;
	static boolean ctrlPressed;
	static KeyCode enter = KeyCode.ENTER;
	static KeyCode pgUp = KeyCode.PAGE_UP;
	static KeyCode pgDown = KeyCode.PAGE_DOWN;
	static KeyCode home = KeyCode.HOME;
	static KeyCode end = KeyCode.END;

	static KeyCode arrowUp = KeyCode.UP;
	static KeyCode arrowRight = KeyCode.RIGHT;
	static KeyCode arrowDown = KeyCode.DOWN;
	static KeyCode arrowLeft = KeyCode.LEFT;

	static KeyCode o = KeyCode.O;
	static boolean oPressed;
	static KeyCode s = KeyCode.S;
	static boolean sPressed;
	static KeyCode z = KeyCode.Z;
	static boolean zPressed;
	static KeyCode n = KeyCode.N;
	static boolean nPressed;
	static KeyCode y = KeyCode.Y;
	static boolean yPressed;
	static KeyCode f = KeyCode.F;
	static boolean fPressed;
	static KeyCode r = KeyCode.R;
	static boolean rPressed;
	static KeyCode m = KeyCode.M;
	static boolean mPressed;
	static KeyCode h = KeyCode.H;
	static boolean hPressed;

	/**
	 * Handles a key press
	 * @param caller - The code text area that triggered the call
	 * @param key - the keycode of the key pressed
	 */
	public static void keyPressed(CodeTextArea caller, KeyCode key){
		//Sets the caller key pressed boolean to true
		if(key == ctrl) ctrlPressed = true;
		if(key == o) oPressed = true;
		if(key == s) sPressed = true;
		if(key == z) zPressed = true;
		if(key == y) yPressed = true;
		if(key == n) nPressed = true;
		if(key == f) fPressed = true;
		if(key == r) rPressed = true;
		if(key == m) mPressed = true;
		if(key == h) hPressed = true;
		
		
		if(ctrlPressed && oPressed) { 	//ctrl + o
			System.out.println("act   Listner: Ctrl + o");
			KeyEvents.open();	//calls keyevents.open
			ctrlPressed = false;
			oPressed = false;
		} else

		if(ctrlPressed && sPressed) {	//ctrl + s
			System.out.println("act   Listner: Ctrl + s");
			KeyEvents.save(caller);	//calls keyevents.save
			ctrlPressed = false;
			sPressed = false;
		} else

		if(ctrlPressed && zPressed) {	//ctrl + z
			System.out.println("act   Listner: Ctrl + z");
			KeyEvents.undo(caller);	//calls keyevents.undo
			zPressed = false;
		} else

		if(ctrlPressed && yPressed) {	//ctrl + y
			System.out.println("act   Listner: Ctrl + y");
			KeyEvents.redo(caller);	//calls keyevents.redo
			yPressed = false;
		} else

		if(ctrlPressed && fPressed) {	//ctrl + f
			System.out.println("act   Listner: Ctrl + f");
			KeyEvents.find(caller);	//calls keyevents.find
			ctrlPressed = false;
			fPressed = false;
		} else

		if(ctrlPressed && hPressed) {	//ctrl + h
			System.out.println("act   Listner: Ctrl + h");
			KeyEvents.findAndReplace(caller);	//calls keyevents.findandreplace
			ctrlPressed = false;
			hPressed = false;
		} else

		if(ctrlPressed && nPressed) {	//ctrl + n
			System.out.println("act   Listner: Ctrl + n");
			KeyEvents.newTab();
			Loader.tabPane.getSelectionModel().select(Loader.tabPane.getTabs().size() -1); //Adds a new tab to the tab pane
			Loader.tabPane.getSelectionModel().getSelectedItem().getContent().requestFocus(); //Requests focus
			nPressed = false;
		} else

		if(ctrlPressed && mPressed) {	//ctrl + m
			System.out.println("act   Listner: Ctrl + m");
			KeyEvents.map(caller);	//calls keyevents.map
			mPressed = false;
		} else

		if((key == arrowUp) || (key == arrowRight) || (key == arrowDown) || (key == arrowLeft)) {	//arrow keys
			caller.calcChange();	//Calculates any change in text
		} else

		if(key == enter){	//enter
			caller.calcChange();	//Calculates any change in text
		}

	}

	/**
	 * Handles a key release
	 * @param caller - The code text area that triggered the call
	 * @param key - the keycode of the key pressed
	 */
	public static void keyReleased(CodeTextArea caller, KeyCode key){
		//Sets the caller key pressed boolean to false
		if(key == ctrl) ctrlPressed = false; else
		if(key == o) oPressed = false; else
		if(key == s) sPressed = false; else
		if(key == z) zPressed = false; else
		if(key == y) yPressed = false; else
		if(key == n) nPressed = false; else
		if(key == f) fPressed = false; else
		if(key == r) rPressed = false; else
		if(key == m) mPressed = false; else
		if(key == h) hPressed = false;
	}

	/**
	 * Handles a mouse click
	 * @param caller - The code text area that triggered the call
	 */
	public static void mouseClicked(CodeTextArea caller) {
		caller.calcChange();//Calculates any change in text
	}
}
