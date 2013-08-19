package parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dependency_tree.Sentence_tree;
import dependency_tree.Node;
import dependency_tree.Triple;
import dependency_tree.Tuple;

public class Parse_file implements IParse_file {
	
	static Hashtable<String,Boolean> voc;
	static Hashtable<String,Boolean> subvoc;
	static Hashtable<String,String> lemma;
	static Reader input_r0;
	static Reader voc_r;
	static Reader subvoc_r;
	static Writer w;
	
	public Parse_file(String input_name0, String output_name, String voc_name, String subvoc_name) throws FileNotFoundException, UnsupportedEncodingException{
		input_r0 = new Reader(input_name0);
		voc_r = new Reader(voc_name);
		subvoc_r = new Reader(subvoc_name);
		w = new Writer(output_name);
		voc = new Hashtable<String,Boolean>();
		subvoc = new Hashtable<String,Boolean>();
		lemma = new Hashtable<String,String>();
	}
	
	/* Based on the generic vocabulary file and the specific vocabulary file, build dictionaries. */
	public void build_dicos() throws IOException{
		String voc_word = voc_r.readl();
		while(voc_word != null){
			voc.put(voc_word, true);
			voc_word = voc_r.readl();
		}
		
		String subvoc_word = subvoc_r.readl();
		while(subvoc_word != null){
			subvoc.put(subvoc_word, true);
			subvoc_word = subvoc_r.readl();
		}
	}
	
	/* Build 2 dico: dico1 = {key:line ID value:tree} and dico2 = {key:dependency value:lines with this dependency}
	 * Use these 2 dico to parse
	 * Build the Sentence_tree based on these dico */
	@Override
	public Tuple<Sentence_tree, String> build_next_sentence_tree() throws IOException{
		// init the 2 dico
		// dico1 contains the ID of the line and the sentence_tree rooted at this ID.
		Hashtable<String, Sentence_tree> dico1 = new Hashtable<String, Sentence_tree>();
		// dico2 contains the ID of the line and all the lines which depend to this ID.
		Map<String, Collection<String>> dico2 = new HashMap<String, Collection<String>>();
		
		// Create a tree which will be the root of multiple roots:
		Sentence_tree super_root = new Sentence_tree(new Node("","",""));
		dico1.put("0", super_root);
		
		// parse the file and fill the 2 dico
		String sentence = "";
		String line = input_r0.readl();
		
		while(line != null && line != "" && !line.equals(",,,,,,,,,,")){ // ",,,,,,,,,," represents an empty line
			String[] splitted_line = line.split(","); // all the attributes are separated by ","
			// Idea: When we detect multiple roots, we add an element with an unused ID
			// and we set its name to "". The multiple root are dependent to this artificial root.
			if(splitted_line[0].equals("0") && dico1.containsKey("0")){
				// Do nothing.
			}
			// Negation verification
			if(!splitted_line[5].contains("neg")){
				dico1.put(splitted_line[0], new Sentence_tree(new Node(splitted_line[2], splitted_line[4], splitted_line[8]))); // [0] = index and [2] = lemma and [8] = function
			}
			else{
				dico1.put(splitted_line[0], new Sentence_tree(new Node(splitted_line[2], splitted_line[4], "neg"))); // [0] = index and [2] = lemma and [8] = function
			}
			// Completion of dico2
			if(dico2.containsKey(splitted_line[7])){
				Collection<String> col = dico2.get(splitted_line[7]);
				col.add(splitted_line[0]);
				dico2.put(splitted_line[7], col);
			}
			else{
				LinkedList<String> col = new LinkedList<String>();
				col.add(splitted_line[0]);
				dico2.put(splitted_line[7], col);
			}
			// comma characters are considered as ""
			if(splitted_line[1].equals("")){
				sentence += ", ";
				lemma.put(",",",");
			}
			// If the word is not a comma, complete the String "sentence" and the dictionary containing lemmas.
			else{
				sentence += splitted_line[1] + " ";
				lemma.put(splitted_line[1], splitted_line[2]);
			}
			line = input_r0.readl();
		}
		
		// Build the Sentence_tree
		int int_index = 0; // the dependency of the line 1
		while(dico1.containsKey(String.valueOf(int_index))){
			String current_index = String.valueOf(int_index);
			Sentence_tree current_st = (Sentence_tree) dico1.get(current_index); // current root
			Collection<String> index_collection = dico2.get(current_index); // Collection of the dependencies
			if(index_collection != null){
				Iterator<String> iter = index_collection.iterator();
				while(iter.hasNext()){
					String index = (String) iter.next();
					Sentence_tree index_st = (Sentence_tree) dico1.get(index);
					current_st.add_child(index_st);
				}
			}
			int_index++;
		}
		
		// return the root
		if(!dico2.isEmpty() && dico2.containsKey("0")){
			return new Tuple<Sentence_tree,String>((Sentence_tree) dico1.get("0"), sentence);
		}
		else return null;
	}
	
	/* Based on the Sentence_tree's built and the two dictionaries, extract relations. */
	@Override
	public Hashtable<String, LinkedList<Triple<String, String, String>>> relations_extraction() throws IOException{
		// Start by building dictionaries
		build_dicos();
		
		// st is the next sentence tree to be built
		Tuple<Sentence_tree,String> st = build_next_sentence_tree();
		
		if(st == null){
			st = build_next_sentence_tree();
		}
		
		// If st is null, then we reached the end of the file
		Hashtable<String, LinkedList<Triple<String,String,String>>> relations = new Hashtable<String,LinkedList<Triple<String,String,String>>>(); 
		while(st != null){
			for(int index = 0; index < st.get_x().get_children().length; index++){
				Sentence_tree current_root = st.get_x().get_children()[index];
				current_root.set_parent(null); // Remove the reference to the super root.
				// Retrieve the interesting subtrees
				LinkedList<Tuple<Sentence_tree,Tuple<String,String>>> good_subs = current_root.interesting_subtree(voc, subvoc);
				while(!good_subs.isEmpty()){
					// For each interesting subtree
					Tuple<Sentence_tree,Tuple<String,String>> current = good_subs.pop();
					String keyword1 = current.get_y().get_x();
					String keyword2 = current.get_y().get_y();
					Hashtable<String, String> result = current.get_x().find_relation(keyword1, keyword2);
					if(result != null){
						LinkedList<Triple<String,String,String>> list;
						if(!relations.containsKey(result.get("rel"))){
							list = new LinkedList<Triple<String,String,String>>();
							list.add(new Triple<String,String,String>(result.get("suj"),result.get("obj"),st.get_y()));
						}
						else{
							list = relations.get(result.get("rel"));
							list.add(new Triple<String,String,String>(result.get("suj"),result.get("obj"),st.get_y()));
						}
						relations.put(result.get("rel"),list);
					}
				}
			}
			st = build_next_sentence_tree();
		}
		
		return relations;
	}
	
	/* Simply merge two dictionaries. */
	public static Hashtable<String, LinkedList<Tuple<String,String>>> merge_dicos(Hashtable<String, LinkedList<Tuple<String,String>>> dico0, Hashtable<String, LinkedList<Tuple<String,String>>> dico1){
		Hashtable<String, LinkedList<Tuple<String,String>>> result = dico0;
		Enumeration<String> keys = dico1.keys();
		
		while(keys.hasMoreElements()){
			String current = keys.nextElement();
			if(result.contains(current)){
				LinkedList<Tuple<String,String>> list0 = result.get(current);
				list0.addAll(list0.size(), dico1.get(current));
				result.put(current, list0);
			}
			else{
				result.put(current, dico1.get(current));
			}
		}
		
		return result;
	}
	
	/* Naive extraction technic based on POS tags. Not used in this master thesis. */
	public void extract_naive() throws IOException {
		build_dicos();
		
		String line = input_r0.readl();
		LinkedList<Tuple<String, String>> sentence = new LinkedList<Tuple<String, String>>(); // Sentence tokens
		String true_sentence = ""; // Sentence in String format
		Hashtable<String, LinkedList<Triple<String, String, String>>> relations = new Hashtable<String, LinkedList<Triple<String, String, String>>>();
		while(line != null && line != ""){
			if(!line.contains(",,,,,,,,,,")){
				// We keep the lemma and the function of the word
				Tuple<String, String> word = new Tuple<String, String>(line.split(",")[2], line.split(",")[3]);
				true_sentence += line.split(",")[1] + " ";
				sentence.addLast(word);
			}
			else{
				Hashtable<String, Tuple<String, String>> extracted_relations = extract_InSentence_naive(sentence);
				Set<String> keys = extracted_relations.keySet();
				for(String key : keys){
					Tuple<String, String> args = extracted_relations.get(key);
					Triple<String, String, String> triple = new Triple<String, String, String>(args.get_x(), args.get_y(), true_sentence);
					LinkedList<Triple<String, String, String>> list;
					if(relations.containsKey(key)){
						list = relations.get(key);
					}
					else{
						list = new LinkedList<Triple<String, String, String>>();
					}
					list.add(triple);
					relations.put(key, list);
				}
				sentence.clear();
				true_sentence = "";
			}
			line = input_r0.readl();
		}
		// print_extraction_method(relations); // <- for evaluation
		print_extraction_method_XML(relations); // <- to visualize the graph (XML format)
	}
	
	/* Sub-method that considers relations based on POS tags. */
	public Hashtable<String, Tuple<String, String>> extract_InSentence_naive(LinkedList<Tuple<String, String>> sentence) {
		Hashtable<String, Tuple<String, String>> relations = new Hashtable<String, Tuple<String, String>>();
		for(int i = 0; i < sentence.size(); i++){
			String relation = "";
			if(voc.containsKey(sentence.get(i).get_x())){
				for(int j = i+1; j < sentence.size(); j++){
					// We consider verbs between interesting terms.
					if(!relation.equals("") && (subvoc.containsKey(sentence.get(j).get_x()) || (subvoc.containsKey(sentence.get(i).get_x()) && voc.containsKey(sentence.get(j).get_x())))){
						relations.put(relation, new Tuple<String, String>(sentence.get(i).get_x(), sentence.get(j).get_x()));
					}
					else if(sentence.get(j).get_y().equals("V")){
						relation += sentence.get(j).get_x() + " ";
					}
				}
			}
		}
		return relations;
	}
	
	/* Extraction based on dependency grammar.
	 * This naive method only takes the path between the two interesting terms. */
	public void extract_naive2() throws IOException {
		build_dicos();
		
		Tuple<Sentence_tree,String> st = build_next_sentence_tree();
		Hashtable<String, LinkedList<Triple<String,String,String>>> relations = new Hashtable<String,LinkedList<Triple<String,String,String>>>(); 
		while(st != null){
			for(int index = 0; index < st.get_x().get_children().length; index++){
				Sentence_tree current_root = st.get_x().get_children()[index];
				current_root.set_parent(null); // Remove the reference to the super root.
				LinkedList<Tuple<Sentence_tree,Tuple<String,String>>> good_subs = current_root.interesting_subtree(voc, subvoc);
				while(!good_subs.isEmpty()){
					Tuple<Sentence_tree,Tuple<String,String>> current = good_subs.pop();
					String keyword1 = current.get_y().get_x();
					String keyword2 = current.get_y().get_y();
					Hashtable<String, String> result = current.get_x().naive2_find_relation_not_root(keyword1, keyword2);
					if(result != null){
						LinkedList<Triple<String,String,String>> list;
						if(!relations.containsKey(result.get("rel"))){
							list = new LinkedList<Triple<String,String,String>>();
							list.add(new Triple<String,String,String>(result.get("suj"),result.get("obj"),st.get_y()));
						}
						else{
							list = relations.get(result.get("rel"));
							list.add(new Triple<String,String,String>(result.get("suj"),result.get("obj"),st.get_y()));
						}
						relations.put(result.get("rel"),list);
					}
				}
			}
			st = build_next_sentence_tree();
		}
		
		// print_extraction_method(relations); // <- for evaluation
		print_extraction_method_XML(relations); // <- to visualize the graph (XML format)
	}

	public void extraction_method() throws IOException {
		Hashtable<String, LinkedList<Triple<String,String,String>>> relations = relations_extraction();
		
		// print_extraction_method(relations); // <- for evaluation
		print_extraction_method_XML(relations); // <- to visualize the graph
	}
	
	/* Print extracted relations in XML format. */
	public void print_extraction_method_XML(Hashtable<String, LinkedList<Triple<String,String,String>>> relations) throws IOException {
		w.write("<?xml version=\"1.0\"?>\n<rdf:RDF\nxmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
		LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> relations_sorted = relation_sort(relations);
		for(int j = 0; j < relations_sorted.size(); j++){
			String rel = relations_sorted.get(j).get_x().replace("relation: ", "").replace("<S>", "S").replace("<O>", "O").replace(" ", "_");
			LinkedList<Triple<String,String,String>> list = relations_sorted.get(j).get_y();
			for(int index = 0; index < list.size(); index++){
				String subj = list.get(index).get_x().replace("sujet:  ", "");
				w.write("<rdf:Description rdf:about=\""+subj+"\">\n");
				String obj = list.get(index).get_y().replace("objet:  ", "");
				w.write("   <"+rel+" rdf:resource=\""+obj+"\" />\n");
				w.write("</rdf:Description>\n");
			}
		}
		w.write("</rdf:RDF>");
	}
	
	/* Print extracted relations in a more readable format. */
	public void print_extraction_method(Hashtable<String, LinkedList<Triple<String,String,String>>> relations) throws IOException {
		w.write("Number of relations: " + relations.size()+"\n");
		System.out.println("Number of relations: " + relations.size());
		LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> relations_sorted = relation_sort(relations);
		for(int j = 0; j < relations_sorted.size(); j++){
			w.write("----------------------"+"\n");
			System.out.println("----------------------");
			w.write(relations_sorted.get(j).get_x() + " : " + String.valueOf(relations_sorted.get(j).get_y().size())+"\n");
			System.out.println(relations_sorted.get(j).get_x() + " : " + String.valueOf(relations_sorted.get(j).get_y().size()));
			w.write("----------------------"+"\n");
			System.out.println("----------------------");
			LinkedList<Triple<String,String,String>> list = relations_sorted.get(j).get_y();
			for(int index = 0; index < list.size(); index++){
				// x = the subject
				// y = the object
				// z = the sentence
				w.write(list.get(index).get_x()+"\n");
				System.out.println(list.get(index).get_x());
				w.write(list.get(index).get_y()+"\n");
				System.out.println(list.get(index).get_y());
				/* Replace the subject by <S> and the object by <O>
				 * Uncomment the following lines to consider patterns.
				 */
				// String pattern = extract_pattern(list.get(index).get_z(), list.get(index).get_x(), list.get(index).get_y());
				// System.out.println("pattern: "+pattern);
				// w.write("pattern: "+pattern+"\n");
				System.out.println(list.get(index).get_z());
				w.write("phrase: "+list.get(index).get_z()+"\n");
				
				w.write("...................."+"\n");
				System.out.println("....................");
			}
			w.write("----------------------"+"\n");
			System.out.println("----------------------");
		}
	}

	/* INPUTS:
	 * complete_setence is the original sentence, with the inflected form of each word.
	 * to_find_S is the list of words composing the subject to find.
	 * to_find_O is the list of words composing the object to find.
	 * replacement is either <S> or <O> */
	public static String extract_pattern(String complete_sentence, String to_find_S, String to_find_O) {
		// ALGO:
		// 1) transform the original sentence into a sentence composed of lemma and tags <DET>, <START> and <END>
		String lemma_sentence = "<START> ";
		String[] complete_sentence_tokens = complete_sentence.split(" ");
		for(int i = 0; i < complete_sentence_tokens.length; i++){
			String current_lemma = lemma.get(complete_sentence_tokens[i]);
			if(current_lemma.equals("le") || current_lemma.equals("un") || current_lemma.equals("son")){
				lemma_sentence += "<DET>";
			}
			else{
				lemma_sentence += current_lemma;
			}
			lemma_sentence += " ";
		}
		lemma_sentence += "<END>";
		
		String[] lemma_sentence_tokens = lemma_sentence.split(" ");
		// 2) find the indexes of the shortest sequence containing all words of to_find_S, no matter the order.
		int start = 0; // start of the sequence
		int end = lemma_sentence_tokens.length; // end of the sequence
		int len = end - start + 1; // current best length of sequence
		Boolean have_solution = false;
		// we need a number of queues equivalent to the number of distinct words in to_find_S, 
		// and each queue has to be fill at minimum the number of time the corresponding word appears in to_find_S.
		Hashtable<String, Tuple<LinkedList<Integer>, Integer>> queues = new Hashtable<String, Tuple<LinkedList<Integer>, Integer>>();
		String[] subject_tokens = to_find_S.split(":  ")[1].split(" ");
		for(int i = 0; i < subject_tokens.length; i++){
			String current_token = subject_tokens[i];
			if(!queues.containsKey(current_token)){
				LinkedList<Integer> queue = new LinkedList<Integer>();
				Tuple<LinkedList<Integer>, Integer> tuple = new Tuple<LinkedList<Integer>, Integer>(queue, 1);
				queues.put(current_token, tuple);
			}
			else{
				Tuple<LinkedList<Integer>, Integer> tuple = queues.get(current_token);
				int num = tuple.get_y();
				tuple.set_y(num + 1);
				queues.put(current_token, tuple);
			}
		}
		// Now that we have the queue, we can apply the algorithm to find such a sequence.
		String first = "";
		for(int i = 0; i < lemma_sentence_tokens.length; i++){
			String current_token = lemma_sentence_tokens[i];
			if(queues.containsKey(current_token)){
				// Add the index of the token in the corresponding queue
				Tuple<LinkedList<Integer>, Integer> tuple = queues.get(current_token);
				LinkedList<Integer> queue = tuple.get_x();
				queue.addLast(i);
				if(!have_solution) have_solution = check_solution(queues);
				
				Tuple<LinkedList<Integer>, Integer> tuple_first;
				LinkedList<Integer> queue_first;
				if(first != ""){
					tuple_first = queues.get(first);
					queue_first = tuple_first.get_x();
					int min = tuple_first.get_y();
					while(first.equals(current_token) && queue_first.size() > min){
						while(queue_first.size() > min){
							queue_first.removeFirst();
							first = new_first_of_sequence(queues);
							tuple_first = queues.get(first);
							queue_first = tuple_first.get_x();
							min = tuple_first.get_y();
						}
						tuple_first = queues.get(first);
						queue_first = tuple_first.get_x();
						min = tuple_first.get_y();
					}
				}
				else{
					first = current_token;
				}
				tuple_first = queues.get(first);
				queue_first = tuple_first.get_x();
				if(have_solution && i - queue_first.getFirst() < len){
					start = queue_first.getFirst();
					end = i;
					len = end - start;
				}
			tuple.set_x(queue);
			queues.put(current_token, tuple);
			}
		}
		
		lemma_sentence = chunking_and_replace(lemma_sentence_tokens, start, end, "<S>");

		// 3) find the indexes of the shortest sequence containing all words of to_find_O, no matter the order.
		lemma.put("<S>", "<S>"); // Because we now consider a sentence composed of the word <S>
		lemma.put("<START>", "<START>"); // Because we now consider a sentence composed of the word <START>
		lemma.put("<END>", "<END>"); // Because we now consider a sentence composed of the word <END>
		lemma_sentence_tokens = lemma_sentence.split(" ");
		start = 0; // start of the sequence
		end = lemma_sentence_tokens.length; // end of the sequence
		len = end - start + 1; // current best length of sequence
		have_solution = false;
		// we need a number of queues equivalent to the number of distinct words in to_find_O, 
		// and each queue has to be fill at minimum the number of time the corresponding word appears in to_find_O.
		queues = new Hashtable<String, Tuple<LinkedList<Integer>, Integer>>();
		String[] object_tokens = to_find_O.split(":  ")[1].split(" ");
		for(int i = 0; i < object_tokens.length; i++){
			String current_token = object_tokens[i];
			if(!queues.containsKey(current_token)){
				LinkedList<Integer> queue = new LinkedList<Integer>();
				Tuple<LinkedList<Integer>, Integer> tuple = new Tuple<LinkedList<Integer>, Integer>(queue, 1);
				queues.put(current_token, tuple);
			}
			else{
				Tuple<LinkedList<Integer>, Integer> tuple = queues.get(current_token);
				int num = tuple.get_y();
				tuple.set_y(num + 1);
				queues.put(current_token, tuple);
			}
		}
		// Now that we have the queue, we can apply the algorithm to find such a sequence.
		first = "";
		for(int i = 0; i < lemma_sentence_tokens.length; i++){
			String current_token = lemma_sentence_tokens[i];
			if(queues.containsKey(current_token)){
				// Add the index of the token in the corresponding queue
				Tuple<LinkedList<Integer>, Integer> tuple = queues.get(current_token);
				LinkedList<Integer> queue = tuple.get_x();
				queue.addLast(i);
				if(!have_solution) have_solution = check_solution(queues);
				
				Tuple<LinkedList<Integer>, Integer> tuple_first;
				LinkedList<Integer> queue_first;
				if(first != ""){
					tuple_first = queues.get(first);
					queue_first = tuple_first.get_x();
					int min = tuple_first.get_y();
					while(first.equals(current_token) && queue_first.size() > min){
						while(queue_first.size() > min){
							queue_first.removeFirst();
							first = new_first_of_sequence(queues);
							tuple_first = queues.get(first);
							queue_first = tuple_first.get_x();
							min = tuple_first.get_y();
						}
						tuple_first = queues.get(first);
						queue_first = tuple_first.get_x();
						min = tuple_first.get_y();
					}
				}
				else{
					first = current_token;
				}
				tuple_first = queues.get(first);
				queue_first = tuple_first.get_x();
				if(have_solution && i - queue_first.getFirst() < len){
					start = queue_first.getFirst();
					end = i;
					len = end - start;
				}
			tuple.set_x(queue);
			queues.put(current_token, tuple);
			}
		}
		
		lemma_sentence = chunking_and_replace(lemma_sentence_tokens, start, end, "<O>");
		
		// 4) We want to cut the pattern in order to have one word at the left of <S> and one word at the right of <O>
		String pattern_cut = cut_the_pattern(lemma_sentence);
		
		return pattern_cut;
	}
	
	/* Cut the pattern to only have the part between <S> and <O>. */
	private static String cut_the_pattern(String lemma_sentence) {
		String[] lemma_sentence_tokens = lemma_sentence.split(" ");
		String final_pattern = "";
		Boolean found = false;
		
		for(int i = 0; i < lemma_sentence_tokens.length; i++){
			if(i+1 < lemma_sentence_tokens.length && lemma_sentence_tokens[i+1].equals("<S>")){
				found = true;
			}
			if(i-1 > 0 && lemma_sentence_tokens[i-1].equals("<O>")){
				final_pattern += lemma_sentence_tokens[i];
				break;
			}
			if(found){
				final_pattern += lemma_sentence_tokens[i] + " ";
			}
			
		}
		
		return final_pattern;
	}

	/* Put the string "replacement" between the indexes "start" and "end" in complete_sentence_tokens. */
	private static String chunking_and_replace(String[] complete_sentence_tokens, int start, int end, String replacement) {
		String element = "";
		for(int i = 0; i < complete_sentence_tokens.length; i++){
			if(i < start || i > end){
				element += complete_sentence_tokens[i];
				if(i < complete_sentence_tokens.length){
					element += " ";
				}
			}
			else if(i == end) {
				element += replacement + " ";
			}
		}
		return element;
	}

	/* Check if we can consider the queues.
	 * We can consider them if there are at least the minimum number of element in each queue.
	 * Each queue has its own minimum. */
	private static Boolean check_solution(Hashtable<String, Tuple<LinkedList<Integer>, Integer>> queues) {
		Boolean solution = true;
		Enumeration<String> words = queues.keys();
		while(words.hasMoreElements()){
			String word = words.nextElement();
			Tuple<LinkedList<Integer>, Integer> tuple = queues.get(word);
			LinkedList<Integer> queue = tuple.get_x();
			int min = tuple.get_y();
			if(queue.size() < min){
				solution = false;
				break;
			}
		}
		return solution;
	}

	/* Compute the new first element in the subsequence of the sentence. */
	private static String new_first_of_sequence(Hashtable<String, Tuple<LinkedList<Integer>, Integer>> queues) {
		Iterator<String> words = queues.keySet().iterator();
		int min = Integer.MAX_VALUE;
		String min_word = "";
		while(words.hasNext()){
			String word = words.next();
			Tuple<LinkedList<Integer>, Integer> tuple = queues.get(word);
			LinkedList<Integer> queue = tuple.get_x();
			if(!queue.isEmpty()){
				int value = queue.getFirst();
				if(value < min){
					min = value;
					min_word = word;
				}
			}
		}
		return min_word;
	}
	
	/* Insert relations by order. */
	public static LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> insert(LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> list, Tuple<String, LinkedList<Triple<String,String,String>>> elem){
		LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> result = new LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>>();
		Boolean added = false;
		for(int index = 0; index < list.size(); index++){
			if(list.get(index).get_y().size() > elem.get_y().size()){
				result.addLast(list.get(index));
			}
			else{
				if(!added) {
					result.addLast(elem);
					result.addLast(list.get(index));
					added = true;
				}
				else result.addLast(list.get(index));
			}
		}
		if(!added) result.add(elem);
		return result;
	}
	
	/* Sort relations to have the one that have the most different arguments first. */
	public static LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> relation_sort(Hashtable<String, LinkedList<Triple<String,String,String>>> relations){
		Set<String> key_set = relations.keySet();
		LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>> result = new LinkedList<Tuple<String, LinkedList<Triple<String,String,String>>>>();
		Iterator<String> iter = key_set.iterator();
		String current = "";
		while(iter.hasNext()){
			current = iter.next();
			result = insert(result, new Tuple<String, LinkedList<Triple<String,String,String>>>(current, relations.get(current)));
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException{		
		if(args.length != 4){
			System.out.println("Please use: ");
			System.out.println("java Parse_file [input_file_path] [output_file_path] [global-voc_file_path] [specific-voc_file_path]");
			System.exit(0);
		}
		
		// Input file name;
		String input_name = args[0];
			
		// Output file name:
		String output_name = args[1];
		
		// Voc file name:
		String voc_name = args[2];
		
		// Subvoc file name:
		String subvoc_name = args[3];
		
		IParse_file parser0 = new Parse_file(input_name, output_name, voc_name, subvoc_name);
		
		parser0.extraction_method(); // The one used in this master thesis (based on dependency grammar)
		//parser0.extract_naive(); // Naive extraction technic based on POS tags. Not used in this master thesis.
		//parser0.extract_naive2(); // Naive extraction technic based on dependency grammar. Only the path is taken.
		
		voc_r.close();
		w.close();
	}
}
