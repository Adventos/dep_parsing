package dependency_tree;

import java.util.Hashtable;
import java.util.LinkedList;

/* Comments about methods of this class are in the interface (ISentence_tree) */

public class Sentence_tree implements ISentence_tree{
	
	Node root;
	Sentence_tree parent;
	Sentence_tree[] children;
	
	public Sentence_tree(){
		this.root = null;
		this.children = new Sentence_tree[0];
	}
	
	public Sentence_tree(Node root){
		this.root = root;
		this.children = new Sentence_tree[0];
	}
	
	@Override
	public Sentence_tree copy(){
		Node the_node = new Node(this.root.name, this.root.POS, this.root.function);
		Sentence_tree the_copy = new Sentence_tree(the_node);
		
		for(int index = 0; index < this.children.length; index++){
			the_copy.add_child(this.children[index].copy());
		}
		
		return the_copy;
	}
	
	@Override
	public void set_root(String name){
		root.set_name(name);
	}
	
	@Override
	public void set_children(Sentence_tree[] children){
		this.children = children;
		for(int index = 0; index < this.children.length; index++){
			this.children[index].parent = this;
		}
	}
	
	@Override
	public void set_parent(Sentence_tree parent){
		this.parent = parent;
	}
	
	@Override
	public String get_root(){
		return root.get_name();
	}
	
	@Override
	public Sentence_tree get_parent(){
		return this.parent;
	}
	
	@Override
	public Sentence_tree[] get_children(){
		return children;
	}
	
	@Override
	public void add_child(Sentence_tree child){
		Sentence_tree[] new_children = new Sentence_tree[this.children.length + 1];
		for(int i = 0; i < this.children.length; i++){
			new_children[i] = this.children[i];
		}
		new_children[this.children.length] = child;
		child.parent = this;
		this.children = new_children;
	}
	
	@Override
	public void add_first_index_child(Sentence_tree child){
		Sentence_tree[] new_children = new Sentence_tree[this.children.length + 1];
		for(int i = 0; i < this.children.length; i++){
			new_children[i + 1] = this.children[i];
		}
		new_children[0] = child;
		child.parent = this;
		this.children = new_children;
	}
	
	@Override
	public void remove_child(Sentence_tree child){
		Sentence_tree[] new_children = new Sentence_tree[this.children.length - 1];
		int j = 0;
		for(int i = 0; i < this.children.length; i++){
			if(this.children[i] != child){ 
				new_children[j] = this.children[i];
				j++;
			}
		}
		this.children = new_children;
	}
	
	@Override
	public LinkedList<Sentence_tree> bfs_name(Hashtable<String,Boolean> voc){
		LinkedList<Sentence_tree> queue = new LinkedList<Sentence_tree>();
		LinkedList<Sentence_tree> voc_nodes = new LinkedList<Sentence_tree>();
		queue.add(this);
		while(!queue.isEmpty()){
			Sentence_tree current = queue.pop();
			if(voc.containsKey(current.root.name)) voc_nodes.add(current);
			for(int index = 0; index < current.children.length; index++){
				queue.addLast(current.children[index]);
			}
		}
		return voc_nodes;
	}
	
	@Override
	public LinkedList<Sentence_tree> bfs_name(String voc){
		LinkedList<Sentence_tree> queue = new LinkedList<Sentence_tree>();
		LinkedList<Sentence_tree> voc_nodes = new LinkedList<Sentence_tree>();
		queue.add(this);
		while(!queue.isEmpty()){
			Sentence_tree current = queue.pop();
			if(voc.equals(current.root.name)) voc_nodes.add(current);
			for(int index = 0; index < current.children.length; index++){
				queue.addLast(current.children[index]);
			}
		}
		return voc_nodes;
	}
	
	@Override
	public Sentence_tree bfs_function(String function){
		LinkedList<Sentence_tree> queue = new LinkedList<Sentence_tree>();
		queue.add(this);
		while(!queue.isEmpty()){
			Sentence_tree current = queue.pop();
			if(current.root.function.equals(function)){
				return current;
			}
			for(int index = 0; index < current.children.length; index++){
				queue.addLast(current.children[index]);
			}
		}
		return null;
	}
	
	@Override
	public Sentence_tree find_common_node(Sentence_tree keyword_tree){
		Sentence_tree temp1 = this;
		LinkedList<Sentence_tree> tree_list1 = new LinkedList<Sentence_tree>();
		Sentence_tree temp2 = keyword_tree;
		LinkedList<Sentence_tree> tree_list2 = new LinkedList<Sentence_tree>();
		while(temp1 != null){
			tree_list1.addLast(temp1);
			temp1 = temp1.get_parent();
		}
		while(temp2 != null){
			tree_list2.addLast(temp2);
			temp2 = temp2.get_parent();
		}
		for(int index1 = 0; index1 < tree_list1.size(); index1++){
			for(int index2 = 0; index2 < tree_list2.size(); index2++){
				if(tree_list1.get(index1) == tree_list2.get(index2)){
					return tree_list2.get(index2);
				}
			}
		}
		return null;
	}
	
	// Return a list of tuple<sub_tree,tuple<keyword1, keyword2>>
	@Override
	public LinkedList<Tuple<Sentence_tree, Tuple<String, String>>> interesting_subtree(Hashtable<String,Boolean> voc, Hashtable<String, Boolean> subvoc){
		LinkedList<Sentence_tree> keyword_list = bfs_name(voc);
		LinkedList<Tuple<Sentence_tree, Tuple<String, String>>> interest = new LinkedList<Tuple<Sentence_tree, Tuple<String, String>>>();
		Hashtable<String,Boolean> already_done = new Hashtable<String,Boolean>();
		
		if(!keyword_list.isEmpty()){
			for(int i = 0; i < keyword_list.size(); i++){
				for(int j = 0; j < keyword_list.size(); j++){
					if(i != j && !already_done.containsKey(keyword_list.get(i).get_root()+" "+keyword_list.get(j).get_root())){
						already_done.put(keyword_list.get(i).get_root()+" "+keyword_list.get(j).get_root(), true);
						Sentence_tree interesting_tree1 = keyword_list.get(i);
						Sentence_tree interesting_tree2 = keyword_list.get(j);
						if(subvoc.containsKey(interesting_tree1.get_root()) || subvoc.containsKey(interesting_tree2.get_root())){
							Sentence_tree sub_tree = interesting_tree1.find_common_node(interesting_tree2).copy();
							interest.add(new Tuple<Sentence_tree,Tuple<String,String>>(sub_tree, new Tuple<String,String>(interesting_tree1.root.name, interesting_tree2.root.name)));
						}
					}
				}
			}
		}
		return interest;
	}

	@Override
	public String infix_toString(){
		String representation = root.name;
		if(children.length != 0){
			representation += "[";
			for(int index = 0; index < children.length; index++){
				representation += children[index].toString();
			}
			representation += "]";
		}
		return representation;
	}
	
	@Override
	public String prefix_toString(){
		int count = 0;
		String representation = "";
		if(children.length != 0 && count/2 <= children.length){
			representation += children[count].prefix_toString();
			count++;
		}
		representation += root.name+ " ";
		if(children.length != 0 && count/2 > children.length){
			representation += children[count].prefix_toString();
			count++;
		}
		return representation;
	}
	
	// The String exception is used to say that a branch which starts with a root name exception has not to be written.
	@Override
	public Tuple<String,Boolean> write_the_path(String exception, String query_type){
		Sentence_tree child_of_st = this;
		Boolean required_type = true;
		if(query_type.equals("rel")) required_type = false; // we need a verb
		String representation = "";
		LinkedList<Sentence_tree> stack = new LinkedList<Sentence_tree>();
		if(!child_of_st.root.function.equals("det") && !child_of_st.root.function.equals("ponct") && !child_of_st.root.name.equals(exception) && !child_of_st.root.get_function().equals("aux_tps")){
			representation += child_of_st.root.name + " ";
			if(child_of_st.root.get_POS().contains("V")) required_type = true;
		}
		else return new Tuple<String,Boolean>("",required_type);
		for(int i = 0; i < child_of_st.children.length; i++){
			stack.addLast(child_of_st.children[i]);
		}
		while(!stack.isEmpty()){
			Sentence_tree current = stack.pop();
			 // discard the branches with det and ponct
			if(!current.root.function.equals("det") && !current.root.function.equals("ponct") && !current.root.name.equals(exception) && !current.root.get_function().equals("aux_tps")){
				representation += current.root.name + " ";
				if(current.root.get_POS().equals("V")) required_type = true;
			
				for(int i = current.children.length-1; i >= 0; i--){
					stack.addFirst(current.children[i]);
				}
			}
			
		}

		return new Tuple<String,Boolean>(representation,required_type);
	}
	
	@Override
	public Sentence_tree transform_tree(String keyword1, String keyword2){
		Hashtable<String,Boolean> keyword = new Hashtable<String,Boolean>();
		keyword.put(keyword2, true);
		int index = 0;
		for(index = 0; index < children.length; index++){
			if(!children[index].bfs_name(keyword).isEmpty()) break; // the branch is found.
		}
		this.parent = this.children[index];
		this.parent.root.set_function("root");
		
		this.children[index].add_child(new Sentence_tree(new Node("être","V","aux_pass"))); // add the passive verb "être"
		this.children[index].add_child(this);
		this.root.set_function("suj");
		
		this.remove_child(this.parent);
		
		return this.parent;
	}
	
	@Override
	public Hashtable<String, String> find_relation(String keyword1, String keyword2){
		Sentence_tree transformed = this;
		if(this.root.get_name().equals(keyword1)){
			transformed = transform_tree(keyword1, keyword2);
		}
		else if(this.root.get_name().equals(keyword2)){
			return null;
		}
		else if(this.root.get_POS().contains("V")){
			// check for the composite past problem
			if(this.root.get_name().equals("être") || this.root.get_name().equals("avoir")){
				for(int index = 0; index < children.length; index++){
					if(children[index].root.get_POS().contains("V") && !children[index].root.get_name().equals("être") && !children[index].root.get_name().equals("avoir")){
						// if we enter in this if, this means that we are in front of a verb which is not "être" or "avoir"
						Sentence_tree futur_root = children[index];
						this.add_child(new Sentence_tree(this.root)); // être or avoir will be child of the futur root
						for(int sub_index = 0; sub_index < futur_root.children.length; sub_index++){
							this.add_child(futur_root.children[sub_index]);
						}
						this.root.set_name(futur_root.root.get_name());
						this.remove_child(futur_root);
					}
				}
			}
		}
		
		return transformed.find_relation_not_root(keyword1, keyword2);
	}
	
	@Override
	public Boolean pertinent(String keyword1, String keyword2){
		for(int index = 0; index < children.length; index++){
			if(children[index].root.get_function().equals("suj")){
				Hashtable<String,Boolean> suj = new Hashtable<String,Boolean>();
				suj.put(keyword1, true);
				if(children[index].bfs_name(suj).isEmpty()) return false;
			}
			else if(children[index].root.get_function().contains("obj")){
				Hashtable<String,Boolean> obj = new Hashtable<String,Boolean>();
				obj.put(keyword2, true);
				if(children[index].bfs_name(obj).isEmpty()) return false;
			}
		}
		return true;
	}
	
	@Override
	public String write_the_aff(){
		Sentence_tree st = this;
		for(int index = 0; index < st.children.length; index++){
			if(st.children[index].root.get_function().equals("aff")) return " se ";
			else if(st.children[index].root.get_function().equals("aux_tps")){
				for(int i = 0; i < st.children[index].children.length; index++){
					if(st.children[index].children[i].root.get_function().equals("aff")) return " se ";
				}
			}
		}
		return "";
	}
	
	@Override
	public String write_the_neg(){
		Sentence_tree st = this;
		for(int index = 0; index < st.children.length; index++){
			if(st.children[index].root.get_function().equals("neg")){
				return " pas ";
			}
		}
		return "";
	}

	@Override
	public String skip_spaces(String str){
		String[] split = str.split(" ");
		if(split.length > 0){
			String output = split[0];
			for(int index = 1; index < split.length; index++){
				if(split[index].length() > 0) output += " " + split[index];
			}
			return output;
		}
		else return "";
	}
	
	@Override
	public Hashtable<String, String> find_relation_not_root(String keyword1, String keyword2){
		if(!pertinent(keyword1, keyword2)) return null;
		Hashtable<String,String> data = new Hashtable<String,String>();
		data.put("suj", "");
		data.put("rel", "");
		data.put("obj", "");
		Boolean we_have_verb = false;
		Boolean subject_already_get = false;
		Boolean object_already_get = false;
		Boolean relation_already_get = false;
		if(root.get_POS().contains("V")) we_have_verb = true;
		
		for(int index = 0; index < children.length; index++){	
			if(!children[index].bfs_name(keyword1).isEmpty() &&  !subject_already_get){
				LinkedList<Sentence_tree> subjs = children[index].bfs_name(keyword1);
				Sentence_tree subj = null;
				if(subjs != null)  subj = subjs.pop();
				
				// write the path from the root to the subject
				Tuple<String,Boolean> path = children[index].write_the_path(keyword1, "rel");
				we_have_verb = we_have_verb | path.get_y();
				if(path.get_x() != ""){
					data.put("rel", path.get_x() + " ");
				}
				data.put("rel", data.get("rel") + "<S> ");
				
				
				// write the path from the subject
				data.put("suj", data.get("suj") + " " + subj.write_the_path("", "suj").get_x());
				subject_already_get = true;
			}
			else if(!children[index].bfs_name(keyword2).isEmpty() && !object_already_get && subject_already_get){
				LinkedList<Sentence_tree> objs = children[index].bfs_name(keyword2);
				Sentence_tree obj = null;
				if(objs != null) obj = objs.pop();
				
				// write the path from the root to the object
				Tuple<String,Boolean> path = children[index].write_the_path(keyword2, "rel");
				we_have_verb = we_have_verb | path.get_y();
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + write_the_neg() + write_the_aff() + root.name);
					relation_already_get = true;
				}
				if(path.get_x() != ""){
					data.put("rel", data.get("rel") + " " + path.get_x());
				}
				data.put("rel", data.get("rel") + " <O>");
				
				// write the path from the object
				data.put("obj", data.get("obj") + " " + obj.write_the_path("", "obj").get_x());
				object_already_get = true;
			}
			else if(!object_already_get && subject_already_get && children[index].root.get_POS().equals("P")){
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + write_the_neg() + write_the_aff() + root.name);
					relation_already_get = true;
				}
				// The idea is to check if there is something interesting, which is not in the path between the root and the object.
				// Here, we check the presence of <REL> <P> <obj> such that: <mettre> <en> <évidence>.
				Sentence_tree child = children[index];
				data.put("rel", data.get("rel") + " " + child.get_root());
				for(int j = 0; j < child.children.length; j++){
					if(child.children[j].root.get_function().equals("obj")){
						data.put("rel", data.get("rel") + " " + child.children[j].get_root());
					}
				}
				
			}
			else if(!object_already_get && subject_already_get && children[index].get_root().equals("être") && !children[index].root.get_function().equals("aux_tps")){
				data.put("rel", data.get("rel") + " " + children[index].get_root());
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + write_the_neg() + write_the_aff() + root.name);
					relation_already_get = true;
				}
			}
		}
		
		// processing to skip the spaces and count the length of the relation (max length = 3)
		data.put("suj", "sujet: " + skip_spaces(data.get("suj")));
		data.put("obj", "objet: " + skip_spaces(data.get("obj")));
		data.put("rel", "relation: " + skip_spaces(data.get("rel")));
		
		// Because we have <S> and <O> in the relation String (which is not part of the relation), and "relation: " we count -3.
		int count = data.get("rel").split(" ").length - 3;
		
		if(!data.get("suj").equals("sujet: ") && !data.get("obj").equals("objet: ") && we_have_verb && count < 8){
			return data;
		}
		else return null;
	}
	
	@Override
	public Hashtable<String, String> naive2_find_relation_not_root(String keyword1, String keyword2){
		Hashtable<String,String> data = new Hashtable<String,String>();
		data.put("suj", "");
		data.put("rel", "");
		data.put("obj", "");
		Boolean subject_already_get = false;
		Boolean object_already_get = false;
		Boolean relation_already_get = false;
		
		for(int index = 0; index < children.length; index++){	
			if(!children[index].bfs_name(keyword1).isEmpty() &&  !subject_already_get){
				LinkedList<Sentence_tree> subjs = children[index].bfs_name(keyword1);
				Sentence_tree subj = null;
				if(subjs != null)  subj = subjs.pop();
				
				// write the path from the root to the subject
				Tuple<String,Boolean> path = children[index].write_the_path(keyword1, "rel");
				if(path.get_x() != ""){
					data.put("rel", path.get_x() + " ");
				}
				data.put("rel", data.get("rel") + "<S> ");
				
				// write the path from the subject
				data.put("suj", data.get("suj") + " " + subj.write_the_path("", "suj").get_x());
				subject_already_get = true;
			}
			else if(!children[index].bfs_name(keyword2).isEmpty() && !object_already_get && subject_already_get){
				LinkedList<Sentence_tree> objs = children[index].bfs_name(keyword2);
				Sentence_tree obj = null;
				if(objs != null) obj = objs.pop();
				
				// write the path from the root to the object
				Tuple<String,Boolean> path = children[index].write_the_path(keyword2, "rel");
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + root.name);
					relation_already_get = true;
				}
				if(path.get_x() != ""){
					data.put("rel", data.get("rel") + " " + path.get_x());
				}
				data.put("rel", data.get("rel") + " <O>");
				
				// write the path from the object
				data.put("obj", data.get("obj") + " " + obj.write_the_path("", "obj").get_x());
				object_already_get = true;
			}
			else if(!object_already_get && subject_already_get){
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + root.name);
					relation_already_get = true;
				}
				// The idea is to check if there is something interesting, which is not in the path between the root and the object.
				// Here, we check the presence of <REL> <P> <obj> such that: <mettre> <en> <évidence>.
				Sentence_tree child = children[index];
				data.put("rel", data.get("rel") + " " + child.get_root());
				for(int j = 0; j < child.children.length; j++){
					if(child.children[j].root.get_function().equals("obj")){
						data.put("rel", data.get("rel") + " " + child.children[j].get_root());
					}
				}
			}
			else if(!object_already_get && subject_already_get){
				data.put("rel", data.get("rel") + " " + children[index].get_root());
				if(!relation_already_get){
					data.put("rel", data.get("rel") + " " + root.name);
					relation_already_get = true;
				}
			}
		}
		
		// processing to skip the spaces and count the length of the relation (max length = 3)
		data.put("suj", "sujet: " + skip_spaces(data.get("suj")));
		data.put("obj", "objet: " + skip_spaces(data.get("obj")));
		data.put("rel", "relation: " + skip_spaces(data.get("rel")));
		
		// Because we have <S> and <O> in the relation String (which is not part of the relation), and "relation: " we count -3.
		int count = data.get("rel").split(" ").length - 3;
		
		if(!data.get("suj").equals("sujet: ") && !data.get("obj").equals("objet: ") && count < 7){
			return data;
		}
		else return null;
	}
	
}
