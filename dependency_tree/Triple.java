package dependency_tree;

public class Triple<X, Y, Z> {
	X x;
	Y y;
	Z z;
	
	public Triple(X x, Y y, Z z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public X get_x(){
		return x;
	}
	
	public Y get_y(){
		return y;
	}
	
	public Z get_z(){
		return z;
	}
	
	public void set_x(X x){
		this.x = x;
	}
	
	public void set_y(Y y){
		this.y = y;
	}
	
	public void set_z(Z z){
		this.z = z;
	}
}
