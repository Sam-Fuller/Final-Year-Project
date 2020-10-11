package Test;

import static org.junit.Assert.*;

import java.util.stream.Stream;

import org.junit.Test;

import Lib.LineList;

public class LineListTest {


	@Test
	public void loadTest() {
		LineList test1 = new LineList();
		test1.reset();

		Stream.iterate(0, x -> x + 1)
		.limit(500)
		.map(x -> "index number " + x)
		.forEachOrdered(x -> test1.load(x));

		assertEquals(500, test1.len());
		assertEquals("index number 0", test1.getLine(000));
		assertEquals("index number 300", test1.getLine(300));
		assertEquals("index number 499", test1.getLine(499));
	}

	@Test
	public void addTest() {
		LineList test2 = new LineList();
		test2.reset();

		Stream.iterate(0, x -> x + 1)
		.limit(500)
		.map(x -> "index number " + x)
		.forEachOrdered(x -> test2.load(x));

		test2.add(0, "This is an additional line");
		test2.add(250, "This is another additional line");
		test2.add(400, "This is a third additional line");

		assertEquals(503, test2.len());
		assertEquals("This is an additional line", test2.getLine(0));
		assertEquals("index number 100", test2.getLine(101));
		assertEquals("index number 300", test2.getLine(302));
		assertEquals("This is another additional line", test2.getLine(250));
		assertEquals("index number 499", test2.getLine(502));
		assertEquals("This is a third additional line", test2.getLine(400));
	}

	@Test
	public void removeTest() {
		LineList test3 = new LineList();
		test3.reset();

		Stream.iterate(0, x -> x + 1)
		.limit(500)
		.map(x -> "index number " + x)
		.forEachOrdered(x -> test3.load(x));

		test3.remove(0);
		test3.remove(200);
		test3.remove(400);

		assertEquals(497, test3.len());
		assertEquals("index number 1", test3.getLine(0));
		assertEquals("index number 300", test3.getLine(298));
		assertEquals("index number 499", test3.getLine(496));
	}

	@Test
	public void undoTest() {
		LineList test5 = new LineList();
		test5.reset();

		Stream.iterate(0, x -> x + 1)
		.limit(500)
		.map(x -> "index number " + x)
		.forEachOrdered(x -> test5.load(x));

		test5.prep();
		test5.add(100, "This is an additional line");
		test5.prep();
		test5.add(250, "This is another additional line");

		assertEquals(502, test5.len());
		assertEquals("This is an additional line", test5.getLine(100));
		assertEquals("This is another additional line", test5.getLine(250));

		test5.undo();

		assertEquals(501, test5.len());
		assertEquals("This is an additional line", test5.getLine(100));
		assertEquals("index number 250", test5.getLine(251));

	}

}
