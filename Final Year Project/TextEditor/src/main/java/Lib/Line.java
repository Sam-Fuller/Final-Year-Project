package Lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
/**
 * A line node
 * When instantiated, line is an immutable linear collection
 * 
 * @author Sam
 *
 */
public class Line {
	private String Content; //The content of the line
	private Line Next = null; //A pointer to the next line
	
	/**
	 * A Constructor that requires the content of the line
	 * @param content - The content of the new line
	 */
	public Line(String content) {
		Content = content;
	}
	
	/**
	 * Sets the content of the line
	 * @param content - The new content of the lineLin
	 */
	public void setContent(String content) {
		Content = content;
	}
	
	/**
	 * Sets the pointer to the next line
	 * @param line - The new next line
	 */
	public void setNext(Line line) {
		Next = line;
	}
	
	/**
	 * Returns the content of a line at relIndex in the structure
	 * @param relIndex - The index from which the content will be returned
	 * @return - The content of the line
	 */
	public String getContent(int relIndex) {
		if (relIndex == 0){
			return Content;
		}else{
			return Next.getContent(relIndex-1);
		}
	}

	/**
	 * Returns the number of lines in the structure
	 * @return - The number of lines in the structure
	 */
	public int len() {
		if (Next != null) {
			return Next.len() + 1;
		}else{
			return 1;
		}
	}

	/**
	 * Returns the structure with the line added at the end
	 * @param line - The line to be added
	 * @return - The new structure with the line appended
	 */
	public Line append(Line line) {
		if (Next == null) {					//If there is no next line
			Line temp = new Line(Content);	//Add the given line as the next line
			temp.Next = line;
			return temp;
			
		}else {								//If there is a next line
			Line temp = new Line(Content);	//Recursively call this function until the correct line has been reached
			temp.Next = Next.append(line);
			return temp;
		}
	}
	
	/**
	 * Returns the structure with the line added at a given index
	 * @param relIndex - The index the new line will be placed
	 * @param line - The line to be added
	 * @return - The new structure with the line added
	 */
	public Line add(int relIndex, Line line) {
		if(relIndex <= 0) {		//If the line is to be added at the start of the data structure
			line.setNext(this);	//Add the line in front of this line
			return line;
				
		}else if(relIndex == 1) { 			//If the line is to be added at the next line
			line.setNext(Next);				//Add the line between this line and the next
			Line temp = new Line(Content);
			temp.Next = line;
			return temp;
			
		}else if(Next == null) {			//If there is no next line
			Line temp = new Line(Content);	//Create new empty lines until the correct index is reached
			temp.setNext(new Line(""));
			temp.Next = temp.Next.add(relIndex-1, line);
			return temp;
			
		}else{								//If the index has not been reached, and there is a next line
			Line temp = new Line(Content);	//Recursively call this function until the correct line is reached
			temp.Next = Next.add(relIndex-1, line);
			return temp;
		}
	}
	
	/**
	 * Returns the structure without the line at a given index
	 * @param relIndex - The index of the line to be removed
	 * @return - The new structure with the line removed
	 */
	public Line remove(int relIndex) {
		if(relIndex == 0) {	//If this is the line to be removed
			return Next;	//Return the next line
			
		}else if(Next == null) {		//If there is no next line
			return new Line(Content);	//Return a new line with this content
			
		}else{								//If the correct line has not been reached, and there is a next line
			Line temp = new Line(Content);	//Recursively call this function until the correct line is reached
			temp.Next = Next.remove(relIndex-1);
			return temp;
		}
	}
	
	/**
	 * Replace a line at a given index with a given line
	 * @param relIndex - The index of the line to be changed
	 * @param line - The line to be stored in its place
	 * @return- The new structure with the new line
	 */
	public Line edit(int relIndex, Line line) {
		if(relIndex == 0) {		//If the index of the line to be replaced has been reached
			line.setNext(Next);	//Replace this line with the one given
			return line;
			
		}else if (Next == null) {	//If the end of the structure has been reached and no line has been changed
			return this;			//Do not change any line
			
		}else{								//If the correct line has not been reached, and there is a next line
			Line temp = new Line(Content);	//Recursively call this function until the correct line is reached
			temp.Next = Next.edit(relIndex-1, line);
			return temp;
		}
	}
	
	/**
	 * Returns the structure in the form of an ArrayList<String>
	 * @return - The structure as a list
	 */
	public List<String> toList() {
		if (Next != null) {								//If the end of the structure has not been reached
			List<String> temp = new ArrayList<String>();//Generate a list of all next lines, add this line and return it
			temp.add(Content);
			temp.addAll(Next.toList());
			return temp;
			
		} else {											//if the end of the strucutre has been reached
			List<String> temp = (new ArrayList<String>());	//Add this line to the list and return it
			temp.add(Content);
			return temp;
		}	
	}
	
	/**
	 * Applies a function to all lines in the list
	 * @param function - The function to be applied
	 * @return - The new structure with the function applied
	 */
	public Line map(Function<String,String> function) {
		Line temp = new Line(function.apply(Content)); //Apply the function to this line
		
		if(Next != null) {					//If there is a next line
			temp.setNext(Next.map(function));//Apply the Function to it
		}
		return temp;
	}
}