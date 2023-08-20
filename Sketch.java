import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/* @author Harry Beesley-Gilman on scaffold code
 * June/1/22
 * Purpose: create a sketch class with some functional methods.
 */

public class Sketch {
    private TreeMap<Integer, Shape> shapes; /*maps ID's to the shapes within them.*/
    /*let's add functions to add, remove, print as a string, and determine what shapes we've clicked on*/
    int id;

    public Sketch (){
        shapes = new TreeMap<Integer, Shape>();
        id = 0;
    }

    public void addShape(int ID, Shape shape){
        shapes.put(ID,shape);
    }



    public void removeShape(int ID){
        if (shapes.containsKey(ID)){
            shapes.remove(ID);}
        else {
            System.out.println("No shape with this ID number");
        }
    }

    public Shape contains(Point p) {
        for (int anID : shapes.keySet()) {
            if (shapes.get(anID).contains(p.x, p.y)) {
                return shapes.get(anID);
            }
        }
        return null;
    }

    public synchronized Shape findShape(Point p) {
        Set <Integer> keys = shapes.descendingKeySet(); //to get the topmost shape
        for(Integer i: keys){
            System.out.println("i option is " + i);
            if(shapes.get(i).contains(p.x, p.y)){
                System.out.println(i + " which id we hit");
                return shapes.get(i);}}
        return null;
    }

    public TreeMap<Integer, Shape> getShapes(){
        return shapes;
        /*just return the treepmap*/
    }

    /*allows you to get the ID from a shape, working backwards.*/
    public int getID(Shape s) {
        Set<Integer> keys = shapes.keySet(); //to get the topmost shape
        for(Integer i: keys)
            if(shapes.get(i).equals(s))
                return i;
        return 0;
    }

    public int changeID(){
        return id++;
    }

    public void recolor(int ID, Color newColor){
        shapes.get(ID).setColor(newColor);
    }

    public void move(int ID, int dx, int dy){
        Shape temp = shapes.get(ID);
        temp.moveBy(dx,dy);
        shapes.put(ID, temp);
    }

    public String toString() {
        String shapeList = "";
        for (Shape s:shapes.values()){
            shapeList = shapeList.concat(s.toString() + "\n");
        }
        return shapeList;
    }
}
