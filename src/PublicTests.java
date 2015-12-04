import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;

/**
 * This class contains the public test cases for the project. The public tests
 * are distributed with the project description, and students can run the public
 * tests themselves against their own implementation.
 * 
 * Any changes to this file will be ignored when testing your project.
 * 
 */
public class PublicTests extends BaseTests {
	static {
		input="data/mini.txt";
		try {
			InvertedIndex.buildInvertedIndex(input, output);
			readInvertedIndex();
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			initializationError = e.toString();
		}
	}
	
	public void testMapper() throws IOException {
		InvertedIndex.Map m = new InvertedIndex.Map();
		MapDriver<Text, Text, Text, PairTextDoubleWritable> mapDriver = MapDriver
				.newMapDriver(m);
		mapDriver
				.withInput(
						new Text("d"),
						new Text(
								"The Cat likes the Mouse very very much, it is a very good cat!!!"));
		mapDriver.withOutput(new Text("cat"), new PairTextDoubleWritable(
				new Text("d"), new DoubleWritable(2. / 5)));
		mapDriver.withOutput(new Text("good"), new PairTextDoubleWritable(
				new Text("d"), new DoubleWritable(1. / 5)));
		mapDriver.withOutput(new Text("like"), new PairTextDoubleWritable(
				new Text("d"), new DoubleWritable(1. / 5)));
		mapDriver.withOutput(new Text("mous"), new PairTextDoubleWritable(
				new Text("d"), new DoubleWritable(1. / 5)));
		mapDriver.runTest();
		--InvertedIndex.corpusSize;
	}

	public void testReducer() throws IOException {
		InvertedIndex.Reduce r = new InvertedIndex.Reduce();
		ReduceDriver<Text, PairTextDoubleWritable, Text, Text> reduceDriver = ReduceDriver
				.newReduceDriver(r);
		List<PairTextDoubleWritable> values = new ArrayList<PairTextDoubleWritable>();
		values.add(new PairTextDoubleWritable(new Text("d1"),
				new DoubleWritable(1. / 5)));
		values.add(new PairTextDoubleWritable(new Text("d2"),
				new DoubleWritable(2. / 9)));
		int saveCorpusSize = InvertedIndex.corpusSize;
		InvertedIndex.corpusSize = 10;
		reduceDriver.withInput(new Text("cat"), values);
		reduceDriver.withOutput(new Text("cat"), new Text(
				"d2:0.51598\td1:0.46439"));
		reduceDriver.runTest();
		InvertedIndex.corpusSize = saveCorpusSize;
	}

	public void testCorpusSize() {
		assertEquals(1000, InvertedIndex.getCorpusSize());
	}

	public void testCorrectStopWord() {
		assertEquals(true, InvertedIndex.isStopWord("to"));
	}

	public void testWrongStopWord() {
		assertEquals(false, InvertedIndex.isStopWord("elephant"));
	}

	public void testInvertedIndexProduced() {
		assertNotNull(index);
	}

	public void testNumberPostingLists() {
          try {
		assertEquals(13172, index.keySet().size());
          } catch (AssertionError e) {
		assertEquals(13020, index.keySet().size());
          }
	}

	public void testTotalNumberPostings() {
		int sum = 0;
		for (String t : index.keySet()) {
			sum += index.get(t).size();
		}
                try {
                  assertEquals(78427, sum);
                } catch (AssertionError e) {
                  assertEquals(78020, sum);
                }
        }

	public void testIndexContainsStemmedWords() {
		assertTrue(index.containsKey("univers"));
	}

	public void testIndexDoesNotContainUnstemmedWords() {
		assertFalse(index.containsKey("university"));
	}

	public void testIndexContainsLowerCaseWords() {
		assertTrue(index.containsKey("hawaii"));
	}

	public void testIndexDoesNotContainUppercaseWords() {
		assertFalse(index.containsKey("Hawaii"));
	}

	public void testSizeOfOnePostingList() {
		Set<InvertedIndex.DocumentWeight> s = index.get("action");
		assertEquals(23, s.size());
	}

	public void testTopWordForOnePostingList() {
		InvertedIndex.DocumentWeight dw = index.get("action").first();
		assertEquals("Kill", dw.documentTitle);
	}

	public void testTopScoreForOnePostingList() {
		InvertedIndex.DocumentWeight dw = index.get("action").first();
		assertEquals(0.3753, dw.weight, .0001);
	}
}
