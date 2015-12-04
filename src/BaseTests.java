import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public abstract class BaseTests extends TestCase {
	protected static String input = null;
	protected static final String output = "data/resultDirectory";
	protected static String initializationError = null;
	protected static HashMap<String, TreeSet<InvertedIndex.DocumentWeight>> index = null;

	static {
		try {
			Locale.setDefault(new Locale("en", "US"));
			InvertedIndex.retrieveStopWords("data/stopWords.txt");
			FileUtils.deleteDirectory(new File(output));
		} catch (IOException e) {
			initializationError = e.toString();
		}
	}
	
	protected static void readInvertedIndex() throws IOException {
		File directory = new File(output);
		File[] contents = directory.listFiles();
		File invertedIndex = null;
	
		for (int i = 0; i < contents.length; ++i)
			if (!contents[i].getName().equals("_SUCCESS")
					&& !contents[i].getName().startsWith("."))
				invertedIndex = contents[i].getAbsoluteFile();
	
		if (invertedIndex == null)
			return;
	
		index = new HashMap<String, TreeSet<InvertedIndex.DocumentWeight>>();
	
		BufferedReader r = new BufferedReader(new FileReader(invertedIndex));
	
		String line;
		while ((line = r.readLine()) != null) {
			String[] parts = line.split("\t");
			String token = parts[0];
			for (int i = 1; i < parts.length; ++i) {
				String[] parts2 = parts[i].split(":");
				TreeSet<InvertedIndex.DocumentWeight> s = index.get(token);
	
				if (s == null) {
					s = new TreeSet<InvertedIndex.DocumentWeight>();
					index.put(token, s);
				}
	
				s.add(new InvertedIndex.DocumentWeight(parts2[0], Double
						.parseDouble(parts2[1])));
			}
		}
	
		r.close();
	}

	public void testIndexBuiltWithNoError() {
		assertNull(initializationError);
	}
}