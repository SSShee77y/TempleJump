package templeKnightGame;

public class Vector2 {
	public double x = 0;
	public double y = 0;
	
	public Vector2() {
		
	}
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void add(Vector2 vec) {
		this.x += vec.x;
		this.y += vec.y;
	}
	
	public void add(double x, double y) {
		this.x += x;
		this.y += y;
	}

}
