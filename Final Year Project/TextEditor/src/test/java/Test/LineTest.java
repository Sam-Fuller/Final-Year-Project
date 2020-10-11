package Test;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import Lib.Line;

public class LineTest {
	
	Line head1 = new Line("this is the first line");
	Line head2 = head1.add(1, new Line("this is a second line"));
	Line head3 = head2.remove(0);
	Line head4 = head2.edit(1, new Line("this is a different second line"));

	@Test
	public void lenTest() {
		assertEquals("Length is not 1 when instanciated", 1, head1.len());
	}

	@Test
	public void addTest() {
		 assertEquals(2, head2.len());
	}
	
	@Test
	public void removeTest() {
		assertEquals(1, head3.len());
		assertEquals("this is a second line", head3.getContent(0));
	}
	
	@Test
	public void editTest() {
		assertEquals(2, head4.len());
		assertEquals("this is a different second line", head4.getContent(1));
	}
	
	@Test
	public void bigTest() {
		
		Optional<Line> head5 = Stream.iterate(0, x -> x + 1)
		.limit(100)
		.map(x -> "index number " + x)
		.map(x -> new Line(x))
		.reduce((x,y) -> x.add(x.len(), y));
		
		//test its all set up correctly
		assertEquals("index number 1", head5.get().getContent(1));
		assertEquals("index number 50", head5.get().getContent(50));
		assertEquals("index number 99", head5.get().getContent(99));
		assertEquals(100, head5.get().len());
		
		//test removing 3 lines
		Line head6 = head5.get().remove(30).remove(60).remove(90);
		assertEquals("index number 1", head6.getContent(1));
		assertEquals("index number 50", head6.getContent(49));
		assertEquals("index number 99", head6.getContent(96));
		assertEquals(97, head6.len());
		
		//test adding a line
		Line head7 = head5.get().add(50, new Line("Hello"));
		assertEquals("index number 1", head7.getContent(1));
		assertEquals("Hello", head7.getContent(50));
		assertEquals("index number 99", head7.getContent(100));
		assertEquals(101, head7.len());
		
		//check head 5 is not changed
		assertEquals("index number 1", head5.get().getContent(1));
		assertEquals("index number 50", head5.get().getContent(50));
		assertEquals("index number 99", head5.get().getContent(99));
		assertEquals(100, head5.get().len());
	}
}