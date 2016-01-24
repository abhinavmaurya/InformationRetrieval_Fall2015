/**
 * @author abhinavmaurya
 * A simple Lucene Implementation for Indexing and Ranking Documents.
 * CS 6200 Fall 2015 - Assignement# 4
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.Chart;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;

public class LuceneImplementation {
	
	private static final Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);
	
	/** CORPUS_PATH contain the path of corpus containing documents */
	private static final String CORPUS_PATH = "corpus/";
	
	/** INDEX_PATH gives the path where index should be created */
	private static final String INDEX_PATH = "index/";
	
	/** QUERY_FILE gives the path of queries */
	private static final String QUERY_FILE = "queries.txt";
	
	public static void main(String[] args) throws IOException, ParseException{
		
		// 1. Create Index of given file/corpus
		IndexWriter writer = initializeIndexWriter(INDEX_PATH);
		IndexFiles indFile = new IndexFiles(writer);
		indFile.indexFile(new File(CORPUS_PATH));
		System.out.println("Total Docs Indexed: " + writer.numDocs());
		writer.close();
		
		// 2. Create Term Frequency Dictionary
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH)));
		TermFrequencyBuilder tfb = new TermFrequencyBuilder(reader);
		Map<String, Integer> term_freq = tfb.buildTermFrequencyDictionary("contents");
		
		// 3. Sort the term freq, print the results and plot graph
        TreeMap<String, Integer> sortedMap = sortByValue(term_freq);
        printTermFrequency(sortedMap);
        Integer totalTerms = (int)reader.getSumTotalTermFreq("contents");
        System.out.println("Total Number of terms in corpus: "+ totalTerms);
        System.out.println("Total Number of Unique terms in corpus: "+ sortedMap.size());
        drawChart(sortedMap, totalTerms);
        
        // 4. Search for the queries, score docs and print result
        BufferedReader br = new BufferedReader(new FileReader(QUERY_FILE));
        ArrayList<String> queries = new ArrayList<String>();
        String line = "";
        while ((line = br.readLine()) != null) {
           queries.add(line);
        }
        br.close();
        
        int j = 1;
        for(String query: queries){
        	String op_file = "q"+j+++"_result";
        	PrintWriter out = new PrintWriter(op_file);
        	out.println("Rank\tDoc_id\tScore\t\tDocument Name");
	        Query q = new QueryParser(Version.LUCENE_47, "contents",
	    			sAnalyzer).parse(query);
	        IndexSearcher searcher = new IndexSearcher(reader);
	    	TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
	    	searcher.search(q, collector);
			
	        System.out.println("Total Hits for Query \""+ query+"\" = "+collector.getTotalHits());
	        ScoreDoc[] hits = collector.topDocs(0, 100).scoreDocs;
	
			// 4. display results
			for (int i = 0; i < hits.length; ++i) {
			    int docId = hits[i].doc;
			    Document d = searcher.doc(docId);
			    out.println((i + 1) + ".\t" + docId + "\t" + hits[i].score + "\t" + d.get("filename"));
			}
			out.close();
        }
	}
	
	/**
	 * @param sortedMap containing term freq pair in descending order of frequency.
	 * @param totalUniqueTerms contains total number of indexed term present in the corpus.
	 * It draws the chart for rank vs probability of terms
	 */
	private static void drawChart(TreeMap<String, Integer> sortedMap, Integer totalUniqueTerms) {
		ArrayList<Number> rank = new ArrayList<Number>();
        ArrayList<Number> probability = new ArrayList<Number>();
        int count = 1;
	    for(Integer value: sortedMap.values()){
	    	rank.add(count++);
	        probability.add((double)value/totalUniqueTerms);
	    }
	    
	    //2. Create list for log rank vs log probability
	    ArrayList<Number> log_rank = new ArrayList<Number>();
        ArrayList<Number> log_probability = new ArrayList<Number>();
        count = 1;
	    for(Integer value: sortedMap.values()){
	    	log_rank.add(Math.log(count++));
	        log_probability.add(Math.log((double)value/totalUniqueTerms));
	    }
	    
	    List<Chart> charts = new ArrayList<Chart>();
	    // Create Chart1 for Rank vs Probability curve
	    Chart chart1 = QuickChart.getChart("Rank vs Probability", "Rank (by decreasing frequency)", 
	    		"Probability (of occurence)", "Zipf's Curve", rank, probability);
	    // Create Chart1 for Log Rank vs Log Probability curve
	    Chart chart2 = QuickChart.getChart("Log Rank vs Log Probability", "Log Rank (by decreasing frequency)", 
	    		"Log Probability (of occurence)", "log-log plot", log_rank, log_probability);
	    charts.add(chart1);
	    charts.add(chart2);
	    
	    // Show the charts
	    new SwingWrapper(charts).displayChartMatrix();
	    
	    try {
			BitmapEncoder.saveBitmap(chart1, "Rank vs Probability", BitmapFormat.JPG);
			BitmapEncoder.saveBitmap(chart2, "Log Rank vs Log Probability", BitmapFormat.JPG);
		} catch (IOException e) {
			System.out.println("Unable to save the graph.");
		}
	    
	}

	/**
	 * Sorts the given unsorted map to create a sorted map.
	 * @param term_freq as unsorted Map of term and frequency pair
	 * @return sorted map
	 */
	public static TreeMap<String, Integer> sortByValue (Map<String, Integer> term_freq) {
		ValueComparator vc =  new ValueComparator(term_freq);
		TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(vc);
		sortedMap.putAll(term_freq);
		return sortedMap;
	}
	
	/**
	 * initializeIndexWriter() function is used to create and instantiate the IndexWriter 
	 * @param indexDir gives the path where index should be created
	 * @return IndexWriter writer
	 * @throws IOException
	 */
	public static IndexWriter initializeIndexWriter(String indexDir) throws IOException{
		
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
			sAnalyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
    
    /**
     * printTermFrequency function prints the Map containing term freq pair
     * @param mp as a Map containing Term frequency
     * @throws FileNotFoundException
     */
    @SuppressWarnings("rawtypes")
	public static void printTermFrequency(Map mp) throws FileNotFoundException {
        Iterator it = mp.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            sb.append(pair.getKey()+"\t\t"+pair.getValue()+"\n");
        }
        PrintWriter pw = new PrintWriter("term_frequency.txt");
        pw.println(sb.toString());
        pw.close();
    }
}


