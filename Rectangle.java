import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 *
 * @author Harry Beesley-Gilman on scaffold code, 6/1/22
 * purpose: rectangle class implementing shape for this projet
 *
 */
public class Rectangle implements Shape {
	private int x1, y1, x2, y2;		// upper left and lower right
	private Color color;
	private int id;

	public Rectangle(int x1, int y1, Color color){
		this.x1=x1;
		this.y1=y1;
		this.x2=x1;
		this.y2=y1;
		this.color=color;
		id = 0;
	}
	public Rectangle(int x1, int y1, Color color, int ID){
		this.x1=x1;
		this.y1=y1;
		this.x2=x1;
		this.y2=y1;
		this.color=color;
		this.id = ID;
	}

	public Rectangle(int x1, int y1, int x2, int y2, Color color){
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
		this.color=color;
		id = 0;
	}
	public Rectangle(int x1, int y1, int x2, int y2, Color color, int ID){
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
		this.color=color;
		this.id = ID;
	}



	@Override
	public void moveBy(int dx, int dy) {
		x1+=dx; x2+=dx;
		y1+=dy; y2+=dy;
	}

	public void setCorners(int x1, int y1, int x2, int y2) {
		// Ensure correct upper left and lower right
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}


	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color=color;
	}
		
	@Override
	public boolean contains(int x, int y) {
		if (!((x>=x1 && x<=x2) || (x<=x1 && x>=x2))){
			return false;
		}
		if (!((y>=y1 && y<=y2) || (y<=y1 && y>=y2))){
			return false;
		}
		return true;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1,y1,x2-x1,y2-y1);
	}

	public int getID(){
		return id;
	}

	public String toString() {
		return "r," + x1 + "," + y1 + "," + x2 + "," + y2 + "," + color.getRGB()+",";
	}


}
