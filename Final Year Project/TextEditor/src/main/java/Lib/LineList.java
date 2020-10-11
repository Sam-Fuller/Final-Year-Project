package Lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An object that handles List of Line Structures
 * When an prep() is called linelist makes a new Line that is placed into the arrayList
 * undo() can then be called to return current to any point that prep() has created
 * 
 * @author Sam
 *
 */
public class LineList{
	ArrayList<Line> VersionList = new ArrayList<Line>(); //A List of all versions of the Line structure
	int current = 0;	//The index of the line in version list that is being displayed	
	
	/**
	 * Default constructor
	 */
	public LineList() {
		VersionList.add(new Line(""));
	}
	
	/**
	 * Clears VersionList
	 */
	public void reset() {
		current = 0;
		VersionList = new ArrayList<Line>();
	}
	
	/**
	 * Removes all Lines in VersionList after current
	 * Used so that any excess Lines Undo() create are removed
	 */
	public void resetCurrent() {
		VersionList = VersionList.stream().limit(current+1).collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns the length of the current Line Structure
	 * @return - Returns the length
	 */
	public int len() {
		return VersionList.get(current).len();
	}

	/**
	 * Used to Load a file into a Line structure
	 * Similar to add, but requires no index appends given strings to the Line structure
	 * @param content - The content of the line to be loaded
	 */
	public void load(String content) {
		this.resetCurrent(); //Resets the current LineList
		current = 0;
		
		if (VersionList.size() == 0) {			//If VersionList is empty
			VersionList.add(new Line(content));	//Add a new line at the start
			
		}else {																		//Else
			VersionList.set(0, VersionList.get(current).append(new Line(content)));	//Add the line at the end of the line structure
		}
	}

	/**
	 * Adds a line of content to the current line structure at index
	 * @param index - The index of the line structure at which the content will be added
	 * @param content - The string to be added
	 */
	public void add(int index, String content) {
		this.resetCurrent(); //Remove all excess line structures in VersionList
		if (VersionList.size() == 0) {			//If there are no line structures in VersionList
			VersionList.add(new Line(content));	//Create a new Line structure
			
		}else {	
			Line tba = VersionList.get(current);
			if(tba == null) {			//If there are no lines in the line structure
				tba = new Line(content);//Create a new line
				
			}else {											//Else
				tba = tba.add(index, new Line(content)); 	//Add the line to the existing line structure
			}
			VersionList.set(current, tba);
		}
	}
	
	/**
	 * Removes a line from the current line structure
	 * @param index	- The index of the line to be removed
	 */
	public void remove(int index) {
		this.resetCurrent();
		VersionList.set(current, VersionList.get(current).remove(index));
	}

	/**
	 * Changes a line in the current line structure at index to the given content
	 * @param index - The index of the line to be changed
	 * @param content - The new content of the line
	 */
	public void edit(int index, String content) {
		this.resetCurrent();
		VersionList.set(current, VersionList.get(current).edit(index, new Line(content)));
	}

	/**
	 * Creates a new undo redo point in VersionList
	 */
	public void prep() {
		this.resetCurrent();
		VersionList.add(VersionList.get(current)); //Add a duplicate line structure to the end of VersionList
		current++; //Change current to the index of the new line structure
	}

	/**
	 * Change the current line structure to be one less in VersionList
	 */
	public void undo() {
		if (current > 0)
			current--;
	}

	/**
	 * Change the current line structure to be one more in VersionList
	 */
	public void redo() {
		if (current < VersionList.size()-1)
			current++;
	}

	/**
	 * Returns the content of the line at index
	 * @param index - The index of the line
	 * @return - The content of the line
	 */
	public String getLine(int index){
		return VersionList.get(current).toList().get(index);
	}

	/**
	 * Returns a version of the current Line structure as an ArrayList<String>
	 * @return - The list version of the current Line structure
	 */
	public List<String> toList() {
		if(VersionList.isEmpty()) {
			VersionList.add(new Line(""));
		}
		return VersionList.get(current).toList();
	}

	/**
	 * Maps a Function<String, String> onto the current line structure
	 * @param function - The function to be mapped
	 */
	public void map(Function<String, String> function){
		this.resetCurrent();
		VersionList.add(VersionList.get(current).map(function));
		current++;
	}
}