/**
 * @author abhinavmaurya
 * To create the term frequency dictionary.
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class TermFrequencyBuilder {
	
	/** reader to read the index*/
	private IndexReader reader;
	
	/**
	 * Constructs the TermFrequencyBuilder object by initializing the reader
	 * @param reader as IndexReader
	 */
	TermFrequencyBuilder(IndexReader reader) {
		this.reader = reader;
	}
	
	/**
	 * Builds the Term and Frequency dictionary
	 * @param fieldName nameof the field which should be read
	 * @return Map<String, Integer> as term and frequency map.
	 * @throws IOException
	 */
	public Map<String, Integer> buildTermFrequencyDictionary(String fieldName) throws IOException{
		
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms(fieldName);
		
		TermsEnum iterator = terms.iterator(null);
		BytesRef byteRef = null;
		HashMap<String, Integer> term_freq = new HashMap<String, Integer>();
        while((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            
        	Term t = new Term("contents", term);
        	term_freq.put(term, (int) reader.totalTermFreq(t));
        }
        return term_freq;
	}
	
}
