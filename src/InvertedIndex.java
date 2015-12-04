import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class InvertedIndex {
	private static final Pattern alphaDigit = Pattern
			.compile("[\\p{L}\\p{N}]+");

	// In a real application, this would be an Apache Zookeeper shared counter
	static int corpusSize = 0;
	static ArrayList<String> stopWord = new ArrayList<String>();

	public static int getCorpusSize() {
		// TODO
		return corpusSize;
	}
	
	public static Comparator<Text> comparator(){
		return new Comparator<Text>(){
			@Override
			public int compare(Text x, Text y) {
				// TODO Auto-generated method stub
				return x.toString().compareTo(y.toString());

			}			
		};

	}

	static class Map extends Mapper<Text, Text, Text, PairTextDoubleWritable> {
		
		@Override
		protected void map(Text key, Text value, Context context){
			// TODO
			int total = 0;
			Comparator<Text> comparator = comparator();
			TreeMap <Text,PairTextDoubleWritable> term = new TreeMap <Text,PairTextDoubleWritable>(comparator);
			ArrayList<String> term_wait = new ArrayList<String>();
			Stemmer stemmer = new Stemmer();
			
			Matcher m = alphaDigit.matcher(value.toString());
			
			while (m.find()) {
				
				String token = m.group();
				token = stemmer.stem(token).toLowerCase();
				if(!isStopWord(token)){
					term_wait.add(token);
					total++;
				}
				// TODO
			}
			
			for(String w:term_wait){
				Text t = new Text(w);
				if(!term.containsKey(t)){
					int term_sum = Collections.frequency(term_wait,w);
					double tfi = (double)term_sum/(double)total;
					DoubleWritable tf = new DoubleWritable();
					tf.set(tfi);
					
					PairTextDoubleWritable temp= new PairTextDoubleWritable(key,tf);
					term.put(t, temp);
				}
			}
			
			
			
			for(Text token:term.keySet())
			{				
				try {
					context.write(token,term.get(token));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// TODO
			corpusSize++;
			
		}

	}
	
	
	static class Reduce extends
			Reducer<Text, PairTextDoubleWritable, Text, Text> {

		
		
		protected void reduce(Text key,
				Iterable<PairTextDoubleWritable> values, Context context) {
			int D = getCorpusSize();
			int id = 0;
			Text result = new Text();
			List<DocumentWeight> dw = new ArrayList<>();
			
			String fileList = "";
	        
			
			for(PairTextDoubleWritable value : values) {
				id++;
				DocumentWeight dwi = new DocumentWeight(value.getFirst().toString(),value.getSecond().get());
				dw.add(dwi);
				//fileList += value.getFirst() + ":" + value.getSecond() + "/" ;
			}
			
			
			/*for (int i = 1; i < dw.size(); i++) {
				//element to be inserted
				DocumentWeight temp = dw.get(i);
				int j;
				for (j = i-1; j>=0; j--) {
					//move
					if(dw.get(j).compareTo(temp) == 1){
						dw.get(j+1).weight = dw.get(j).weight;
					}
					else{
						break;
					}
				}
				dw.get(j+1).weight = temp.weight;
			}*/	
			
			
			double idf = Math.log((double)D / (double)id)/Math.log(2);
			double factor = 1e5;
			
			for(int i=0;i<id;i++){
				double tf = dw.get(i).weight;
				double tfidf = tf*idf;
				tfidf = Math.round(tfidf*factor)/factor;
				String tit = dw.get(i).documentTitle;
				dw.set(i, new DocumentWeight(tit,tfidf));
			}
			Collections.sort(dw);
			
			int j = 0;
			for(DocumentWeight d:dw){
				if(j!=0) fileList +="\t";
				fileList += d.documentTitle +":" + d.weight;
				j++;
			}
			
			
			
			result.set(fileList);
	        
			try {
				context.write(key, result);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			// TODO
		}
	}

	public static void retrieveStopWords(String filename) throws IOException {
		try {
			// TODO
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			String strLine;
			Stemmer stemmer = new Stemmer();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				stopWord.add(stemmer.stem(strLine));
			}
			//Close the input stream
			fileReader.close();
		} 
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

	public static boolean isStopWord(String token) {
		// TODO
		if(stopWord.contains(token)){
			return true;
		}
		else{
			return false;
		}
	}

	public static void buildInvertedIndex(String input, String output)
			throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(PairTextDoubleWritable.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		job.waitForCompletion(false);
	}
	
	public static class DocumentWeight implements Comparable<DocumentWeight>
	{
		String documentTitle;
		double weight;
		
		DocumentWeight(String t, double d)
		{
			documentTitle=t;
			weight=d;
		}
		
		@Override
		public int compareTo(DocumentWeight that) {
			// TODO Auto-generated method stub
			if(this.weight>that.weight)
				return -1;
			else if(this.weight<that.weight)
				return 1;
			else
				return this.documentTitle.compareTo(that.documentTitle);
		}
	}
}
