package dependency_tree;

import java.util.Hashtable;
import java.util.LinkedList;

public interface ISentence_tree {

	// Create a copy of the Sentence_tree
	public abstract Sentence_tree copy();

	// Change the name of the root of this Sentence_tree
	public abstract void set_root(String name);

	// Change the set of children of this Sentence_tree root
	public abstract void set_children(Sentence_tree[] children);

	// Change the parent of this Sentence_tree root
	public abstract void set_parent(Sentence_tree parent);

	// Get this root name
	public abstract String get_root();

	// Get this root parent
	public abstract Sentence_tree get_parent();

	// Get this root children
	public abstract Sentence_tree[] get_children();

	// Add a child to the end of the list of children of this root
	public abstract void add_child(Sentence_tree child);

	// Add a child to the beginning of the list of children of this root
	public abstract void add_first_index_child(Sentence_tree child);

	// Remove a given child to the list of this root
	public abstract void remove_child(Sentence_tree child);

	// Retrieve a set of words (stored in a hash table) in the Sentence_tree
	public abstract LinkedList<Sentence_tree> bfs_name(Hashtable<String, Boolean> voc);

	// Retrieve a word in the Sentence_tree
	public abstract LinkedList<Sentence_tree> bfs_name(String voc);

	// Retrieve the function of a word in the Sentence_tree
	public abstract Sentence_tree bfs_function(String function);

	// Find the first (bottom-up) common ancestor between this Sentence_tree and another given Sentence_tree
	public abstract Sentence_tree find_common_node(Sentence_tree keyword_tree);

	// Find a set of interesting subtree in this Sentence_tree
	// An interesting Sentence_tree is a Sentence_tree rooted at the first common ancestor between two interesting terms 
	// Return a list of tuple<sub_tree,tuple<keyword1, keyword2>>
	public abstract LinkedList<Tuple<Sentence_tree, Tuple<String, String>>> interesting_subtree(
			Hashtable<String, Boolean> voc, Hashtable<String, Boolean> subvoc);

	// Print the Sentence_tree in an infix order
	public abstract String infix_toString();

	// Print the Sentence_tree in a prefix order
	public abstract String prefix_toString();

	// Retrieve all the words in the subtrees of this Sentence_tree
	// The String exception is used to say that a branch which starts with a root name exception has not to be written.
	// The string query_type specify if we look for the relation, the subject or the object.
	// If we look for the relation, we have to find a verb (this is the reason why we also return a Boolean)
	public abstract Tuple<String, Boolean> write_the_path(String exception,
			String query_type);

	// Transform this tree, as specified in the theory.
	public abstract Sentence_tree transform_tree(String keyword1,
			String keyword2);

	// Global method to find the relation between two words in this Sentence_tree
	public abstract Hashtable<String, String> find_relation(String keyword1,
			String keyword2);

	// Return true if the relation between the two keywords in this Sentence_tree is pertinent
	public abstract Boolean pertinent(String keyword1, String keyword2);

	// Return the affix of the relation if it exists
	public abstract String write_the_aff();

	// Return the negation of the relation if it exists
	public abstract String write_the_neg();

	// Return the same relation String but with only one space character between two words
	public abstract String skip_spaces(String str);

	// Find the relation between two keywords in this Sentence_tree after the tree being transformed
	public abstract Hashtable<String, String> find_relation_not_root(
			String keyword1, String keyword2);

	public abstract Hashtable<String, String> naive2_find_relation_not_root(String keyword1, String keyword2);

}