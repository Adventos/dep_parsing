package dependency_tree;

public class Node {
	
	String name;
	String POS;
	String function;
	
	public Node(String name, String POS, String function){
		this.name = name;
		this.POS = POS;
		this.function = function;
	}
	
	public void set_name(String name){
		this.name = name;
	}
	
	public void set_POS(String POS){
		this.POS = POS;
	}
	
	public void set_function(String function){
		this.function = function;
	}
	
	public String get_name(){
		return name;
	}
	
	public String get_POS(){
		return POS;
	}
	
	public String get_function(){
		return function;
	}
}