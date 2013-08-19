package dependency_tree;

public class Tuple<X, Y> {

	X x;
	Y y;
	
	public Tuple(X x, Y y){
		this.x = x;
		this.y = y;
	}
	
	public X get_x(){
		return x;
	}
	
	public Y get_y(){
		return y;
	}
	
	public void set_x(X x){
		this.x = x;
	}
	
	public void set_y(Y y){
		this.y = y;
	}
	
}
