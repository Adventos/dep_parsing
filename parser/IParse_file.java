package parser;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import dependency_tree.Sentence_tree;
import dependency_tree.Triple;
import dependency_tree.Tuple;

public interface IParse_file {

	// Based on the generic vocabulary file and the specific vocabulary file, build dictionaries.
	public abstract void build_dicos() throws IOException;

	// Each time this method is called, the next sentence (in the format given by the malt parser) of the corpus 
	// is transformed into a Sentence_tree.
	public abstract Tuple<Sentence_tree, String> build_next_sentence_tree()
			throws IOException;

	// Based on the Sentence_tree's built and the two dictionaries, extract relations.
	public abstract Hashtable<String, LinkedList<Triple<String, String, String>>> relations_extraction()
			throws IOException;

	public abstract void extract_naive() throws IOException;

	public abstract void extract_naive2() throws IOException;
	
	public abstract void print_extraction_method(Hashtable<String, LinkedList<Triple<String,String,String>>> relations) throws IOException;

	public abstract void extraction_method() throws IOException;

}