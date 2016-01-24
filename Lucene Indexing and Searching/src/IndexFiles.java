/**
 * @author abhinavmaurya
 * To indexes the Files
 */

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;

public class IndexFiles {
	
	private IndexWriter writer;
	public static final String CONTENTS_FIELD = "contents";
	public static final String FILENAME_FIELD = "filename";
	
	/**
	 * Constructor to instantiate IndexFiles object
	 * @param writer
	 */
	public IndexFiles(IndexWriter writer) {
		this.writer = writer;
	}
	
	/**
	 * Index the given list of files
	 * @param files as List of Files List<File>
	 */
	public void indexFiles(List<File> files){
		
		for(File f: files){
			indexFile(f);
		}
	}
	
	/**
	 * Index the given File by identifying its type.
	 * @param file as File
	 */
	public void indexFile(File file){
		
		if (!file.exists()) {
		    System.out.println(file + " does not exist.");
		}else if (file.isDirectory()) {
		    for (File f : file.listFiles()) {
		    	indexFile(f);
		    }
		} else{
		    String filename = file.getName().toLowerCase();
		    if (filename.endsWith(".htm") || filename.endsWith(".html")
			    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
		    		createIndexForFile(file);
		    } else{
		    	System.out.println("Skipped " + filename);
		    }
		}
	}
	
	/**
	 * Generates index for the file f
	 * @param f as File
	 */
	public void createIndexForFile(File f){
	    try {
			Document doc = new Document();
	
			String content = new String(Files.readAllBytes(Paths.get(f.getPath())), StandardCharsets.UTF_8);
			String filename = f.getName().toLowerCase();
			// if file is html file then strip off tags from content
			if (filename.endsWith(".htm") || filename.endsWith(".html")){
				content = Jsoup.parse(content).text();
			}
			doc.add(new TextField(CONTENTS_FIELD, content, Field.Store.YES));
			doc.add(new StringField(FILENAME_FIELD, f.getName(), Field.Store.YES));
	
			writer.addDocument(doc);
	    } catch (Exception e) {
	    	System.out.println("Could not add: " + f);
	    }
	}

}
