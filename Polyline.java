import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * @author Harry Beesley-Gilman on scaffold code, 6/1/22
 * purpose: put together freehand polylines
 *
 */
public class Polyline implements Shape {
	public ArrayList<Segment> segments;
	public int id;
	public Color color;

	public Polyline(int x1, int y1, int x2, int y2, Color color, int id) {
		this.id = id;
		Segment newSegment = new Segment(x1, y1, x2, y2, color);
		segments = new ArrayList<Segment>();
		segments.add(newSegment);
		this.color = color;

	}
	public Polyline(int x1, int y1, int x2, int y2, Color color) {
		this.id = 0;
		Segment newSegment = new Segment(x1, y1, x2, y2, color);
		segments = new ArrayList<Segment>();
		segments.add(newSegment);
		this.color = color;

	}

	public Polyline(Color color, int id){
		segments = new ArrayList<Segment>();
		this.color = color;
		this.id = id;
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Segment b : segments){
			b.moveBy(dx,dy);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		for (Segment b : segments){
			b.setColor(color);
	}
	}
	public void setCorners(int x1, int y1, int x2, int y2) {
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments.add(s);
	}
	/*does a point fit along the polyline*/
	@Override
	public boolean contains(int x, int y) {
		/*becuase it's a polyline we must loop through segments*/
		for (Segment b : segments){
			if (b.contains(x,y)){
				return true;
			}
		}
		return false;
	}
	/*draw all segments*/
	@Override
	public void draw(Graphics g) {
		for (Segment b : segments){
			b.draw(g);
		}
	}

	public void addSegment(Segment s) {
		segments.add(s);
	}

	public int getID(){
		return id;
	}

	@Override
	public String toString() {
		String stringy=  new String("p,");
		for(Segment s : segments) {
			stringy = stringy.concat(s.getCornerList()+",");
		}
		stringy = stringy.concat(color.getRGB() + ",");
		return stringy;
	}

}
