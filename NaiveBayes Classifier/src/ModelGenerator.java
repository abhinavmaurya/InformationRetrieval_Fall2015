import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Abhinav-Kartik
 */
public class ModelGenerator {
	
	public static final int NEG_CLASS_ID = 0;
	public static final int POS_CLASS_ID = 1;
	
    private static HashMap<String, Integer> neg_reviews = new HashMap<>();
    private static HashMap<String, Integer> pos_reviews = new HashMap<>();
    
    @SuppressWarnings("resource")
	public HashMap<String, Integer> train(List<File> file_list){
        String[] list;
        HashMap<String, Integer> reviews = new HashMap<>();
	    for (File file : file_list){    
	        if (file.isFile()){
	            try{
	                Scanner sc = new Scanner(file);
	                while(sc.hasNextLine()){
	                    String review_words= sc.nextLine().trim();
	                    list=review_words.split(" ");
	                    for(int i=0;i<list.length;i++){
	                    	if(isValidWord(list[i])){
		                        String word = list[i].trim();
		                        if(reviews.containsKey(word)){
		                            int count = reviews.get(word);
		                            count+= 1;
		                            reviews.put(word, count);
		                        }
		                        else{
		                            reviews.put(word, 1);
		                        }
	                    	}
	                    }
	                }
	            }
	            catch(Exception e)
	            {
	                System.out.println(e);
	            }
	        }
	    
	    }
	    return reviews;
    }
    
    public boolean isValidWord(String wrd){
    	wrd = wrd.trim();
    	if(wrd == null || wrd.isEmpty() || wrd.equals("")){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    public HashMap<String, List<Double>> calculateAndGenerateModel(HashMap<String, Integer>neg_reviews, 
    		HashMap<String, Integer>pos_reviews) throws FileNotFoundException{
    	
    	PrintWriter out = new PrintWriter("model-file");
    	Set<String> neg_terms = neg_reviews.keySet();
        Set<String> pos_terms = pos_reviews.keySet();
        List<String> terms = new ArrayList<>();
        terms.addAll(neg_terms);
        terms.addAll(pos_terms);
        Set<String> all_terms = new HashSet<String>(terms);
        
        HashMap<String, List<Double>> term_class_prob = new HashMap<>();
        
        // Calculate probabilities
        int total_pos_wrds = findTotalWordsForClass(pos_reviews);
        int total_neg_wrds = findTotalWordsForClass(neg_reviews);
        int total_unique_words = all_terms.size();
        String header = String.format("%1$20s"+" " + "%2$20s" +" "+ "%3$20s", "Term", "P(neg)", "P(pos)");
        out.println(header);
        
        for(String term: all_terms){
        	// for class neg
        	double tf_in_neg = 0.0;
        	if(neg_reviews.containsKey(term)){
        		tf_in_neg = neg_reviews.get(term);
        	}
        	double p_term_neg = (tf_in_neg + 1)/(total_neg_wrds + total_unique_words);
        	
        	// for class pos
        	double tf_in_pos = 0.0;
        	if(pos_reviews.containsKey(term)){
        		tf_in_pos = pos_reviews.get(term);
        	}
        	double p_term_pos = (tf_in_pos + 1)/(total_pos_wrds + total_unique_words);
        	
        	List<Double> probs = new LinkedList<Double>();
        	probs.add(p_term_neg);
        	probs.add(p_term_pos);
        	
        	term_class_prob.put(term, probs);
        	String toWrite = String.format("%1$20s"+" " + "%2$20s" +" "+ "%3$20s", term, String.format("%.15f", p_term_neg), String.format("%.15f", p_term_pos));
        	out.println(toWrite);
        }
        out.close();
        out.flush();
        return term_class_prob;
    }
    
    
    public int findTotalWordsForClass(HashMap<String, Integer> review){
    	
    	int total = 0;
    	for(String term: review.keySet()){
    		total += review.get(term);
    	}
    	return total;
    }
    
    
    
    public void filterReviews(){
    	Set<String> neg_terms = neg_reviews.keySet();
        Set<String> pos_terms = pos_reviews.keySet();
        List<String> terms = new ArrayList<>();
        terms.addAll(neg_terms);
        terms.addAll(pos_terms);
        for(int i=0;i<terms.size();i++){
            String word = terms.get(i).toString();
            int n_freq=0;
            int p_freq=0;
            if(neg_reviews.containsKey(word))
                n_freq = neg_reviews.get(word);

            if(pos_reviews.containsKey(word))
            	p_freq=pos_reviews.get(word);
            
            if(n_freq + p_freq < 5){
            	neg_reviews.remove(word);
                pos_reviews.remove(word);
            }
        }
    }
    
    public List<File> fetchFilesForClass(String training_dir, int class_id){
    	File class_type;
    	switch(class_id){
    		
    	case NEG_CLASS_ID:
    		class_type = new File(training_dir + "/neg");
    		break;
    	case POS_CLASS_ID:
    		class_type = new File(training_dir + "/pos");
    		break;
    	default:
    		System.out.println("Unable to identify the class");
    		return null;
    	}
    	
    	File[] file_list = class_type.listFiles();
    	return Arrays.asList(file_list);
    }
    
    public static void main(String[] args) throws FileNotFoundException {
    	
        ModelGenerator tc= new ModelGenerator();
        if(args != null && args.length > 0){
        	String training_dir = args[0];
        	List<File> neg_files = tc.fetchFilesForClass(training_dir, NEG_CLASS_ID);
        	List<File> pos_files = tc.fetchFilesForClass(training_dir, POS_CLASS_ID);
        	neg_reviews = tc.train(neg_files);
        	pos_reviews = tc.train(pos_files);
        	tc.filterReviews();
        	HashMap<String, List<Double>> model = tc.calculateAndGenerateModel(neg_reviews, pos_reviews);
        	tc.printRatio(model);
        }else{
        	System.out.println("Please provide directory for training data");
        }
    }
    
    public void printRatio(HashMap<String, List<Double>> model) throws FileNotFoundException{
    	
    	HashMap<String, Double> pos_to_neg = new HashMap<>();
    	HashMap<String, Double> neg_to_pos = new HashMap<>();
    	
    	for(String term: model.keySet()){
    		Double p2n = Math.log(model.get(term).get(POS_CLASS_ID)) - Math.log(model.get(term).get(NEG_CLASS_ID));
    		Double n2p = Math.log(model.get(term).get(NEG_CLASS_ID)) - Math.log(model.get(term).get(POS_CLASS_ID));
    		pos_to_neg.put(term, p2n);
    		neg_to_pos.put(term, n2p);
    	}
    	
    	TreeMap<String, Double> sorted_p2n = sortByValue(pos_to_neg);
    	TreeMap<String, Double> sorted_n2p = sortByValue(neg_to_pos);
    	

    	printMap(sorted_p2n, "pos_to_neg_ratio");
    	printMap(sorted_n2p, "neg_to_pos_ratio");
    	
    }
    
    @SuppressWarnings("rawtypes")
	public void printMap(TreeMap<String, Double> sortedMap, String filename) throws FileNotFoundException{
    	PrintWriter out = new PrintWriter(filename);
    	String header = String.format("%1$20s"+"\t%2$20s", "Term", filename);
    	out.println(header);
    	Iterator it = sortedMap.entrySet().iterator();
    	int counter = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String toWrite = String.format("%1$20s"+ "\t%2$20s", pair.getKey(), String.format("%.15f", pair.getValue()));
            out.println(toWrite);
            counter++;
            if(counter == 20)
            	break;
        }
        out.close();
    	out.flush();
    }
    
	public static TreeMap<String, Double> sortByValue (Map<String, Double> term_freq) {
		ValueComparator vc =  new ValueComparator(term_freq);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(term_freq);
		return sortedMap;
	}
}
