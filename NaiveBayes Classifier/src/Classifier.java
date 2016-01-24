/**
 * @author Abhinav-Kartik
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Classifier {
	
	HashMap<String, List<Double>> model = new HashMap<>();
	static List<String> neg_files_read = new ArrayList<>();
	static List<String> pos_files_read = new ArrayList<>();
	static List<String> neg_files = new ArrayList<>();
	static List<String> pos_files = new ArrayList<>();
	public static final int NEG_CLASS_ID = 0;
	public static final int POS_CLASS_ID = 1;
	
	@SuppressWarnings("resource")
	public void generateModel(File model_file) throws FileNotFoundException{
		
		if(model_file.exists()){
			Scanner sc = new Scanner(model_file);
			sc.nextLine(); // ignore the header
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				List<String> list = new ArrayList<>();
				for(String i: line.split(" ")){
					if(!i.isEmpty()){
						list.add(i);
					}
				}
				String term = list.remove(0);
				List<Double> probs = new ArrayList<>();
				for(String p: list){
					probs.add(Double.valueOf(p));
				}
				model.put(term, probs);
			}
		}else{
			System.out.println("Please provide valid model file name");
		}
	}
	
	public void predictClassForEachFile(List<File> files) throws FileNotFoundException{
		
		PrintWriter out = new PrintWriter("prediction-file");
		String header = String.format("%1$10s" + "\t%2$20s" + "\t%3$20s", "Doc", "P(neg)", "P(pos)");
        out.println(header);
		for(File f: files){
			predictClass(f, out);
		}
		out.close();
		out.flush();
	}
	
	@SuppressWarnings("resource")
	public void predictClass(File f, PrintWriter out) throws FileNotFoundException{
		
		Scanner sc = new Scanner(f);
		HashMap<String, Integer> tf = new HashMap<>();
		while(sc.hasNextLine()){
			String line = sc.nextLine().trim();
			String[] terms = line.split(" ");
			for(String term: terms){
				//System.out.print(term + ", ");
				if(tf.containsKey(term)){
                    int count = tf.get(term);
                    tf.put(term, count++);
                }
                else{
                    tf.put(term, 1);
                }
			}
		}
		// iterate over all terms to calculate class prob
		double p_doc_pos = 1.0;
		double p_doc_neg = 1.0;
		for(String term: tf.keySet()){
			if(model.containsKey(term)){
				int t_freq = tf.get(term);
				double t_prob_neg = model.get(term).get(NEG_CLASS_ID);
				double t_prob_pos = model.get(term).get(POS_CLASS_ID);
				p_doc_neg += Math.log(t_prob_neg) * t_freq;
				p_doc_pos += Math.log(t_prob_pos) * t_freq;
			}
		}
		if(p_doc_neg > p_doc_pos){
			neg_files.add(f.getName());
		}else {
			pos_files.add(f.getName());
		}
		
		String toWrite = String.format("%1$10s" + "\t%2$20s" + "\t%3$20s", f.getName(), String.format("%.15f", p_doc_neg), String.format("%.15f", p_doc_pos));
    	out.println(toWrite);
		
	}
	
	public static void main(String args[]) throws FileNotFoundException{
		
		Classifier tc= new Classifier();
        if(args != null && args.length == 2){
        	String model_file = args[0];
        	String test_dir = args[1];
        	
        	tc.generateModel(new File(model_file));
        	List<File> bucket = new ArrayList<>();
        	tc.predictClassForEachFile(tc.readFile(new File(test_dir), bucket));
        	
        	if(test_dir.endsWith("dev")){
        		tc.findCorrectClassification();
        	}else{
        		System.out.println("Total Pos Files classified: "+ pos_files.size());
                System.out.println("Total Neg Files classified: "+ neg_files.size());
        	}
        }else{
        	System.out.println("Please provide model file and test directory");
        }
        
	}
	
	public void findCorrectClassification(){
		int neg_count = 0;
		int pos_count = 0;
		for(String f: neg_files){
			if(neg_files_read.contains(f))
				neg_count++;
		}
		
		for(String f: pos_files){
			if(pos_files_read.contains(f))
				pos_count++;
		}
		
		System.out.println("Total Pos Files classified correctly in dev: "+ pos_count);
        System.out.println("Total Neg Files classified correctly in dev: "+ neg_count);
		
	}
	
	public List<File> readFile(File file, List<File> bucket){
		
		if (!file.exists()) {
		    System.out.println(file + " does not exist.");
		}else if (file.isDirectory()) {
		    for (File f : file.listFiles()) {
		    	readFile(f, bucket);
		    }
		} else if(file.getName().endsWith(".txt")){
			if(file.getParentFile().getName().equals("pos")){
				pos_files_read.add(file.getName());
			}else if(file.getParentFile().getName().equals("neg")){
				neg_files_read.add(file.getName());
			}
			bucket.add(file);
			
		}
		return bucket;
	}
}
