import java.awt.*;
import java.util.ArrayList;

/* @author Harry Beesley-Gilman on scaffold code, 6/1/22
 * purpose: Parse messages to extract meaning
 */

public class MessageParser {
    private int x1, x2, y1, y2;
    private int color;
    private int dx, dy;
    private int id;
    private String shapeName;
    private String action;
    private int maxID=1;
    ArrayList<Segment> polyline;

    /*this object is designed to parse a line and store the information in instance variables.*/
    public void parse(String stringy){
        action = "a";
        System.out.println("is parser we are parsing " + stringy);

        String [] components = stringy.split(",");

        /*the 0 index chooses our action, the next generally chooses the shape.
         * Later ones determine x and y coordinates, etc.*/
        if (components.length>0) {
            if (components[0].contentEquals("a") & !(components[1].contentEquals("p"))) {

                action = "a";
                System.out.println("action is " + action);
                if (components[1].contentEquals("r")) {
                    shapeName = "rectangle";
                }
                if (components[1].contentEquals("s")) {
                    shapeName = "segment";
                }
                if (components[1].contentEquals("e")) {
                    shapeName = "ellipse";
                }

                x1 = Integer.parseInt(components[2]);
                y1 = Integer.parseInt(components[3]);
                x2 = Integer.parseInt(components[4]);
                y2 = Integer.parseInt(components[5]);
                color = Integer.parseInt(components[6]);


                id = Integer.parseInt(components[7]);
            }


        /*if we're adding and it's a polyline (else against the not statement above)*/
        else if (components[0].contentEquals("a")){
            polyline = new ArrayList<Segment>();
            System.out.println("we've been triggered and the last term is " + components[components.length-2]);
            action = "a";
            shapeName="polyline";


            /*cycle through components and assemble segments to add to our line*/
            for (int i = 2; i+3<components.length-2;i+=4){
                Segment currentSegment = new Segment(Integer.parseInt(components[i]),Integer.parseInt(components[i+1]),
                        Integer.parseInt(components[i+2]),Integer.parseInt(components[i+3]),new Color(Integer.parseInt(components[components.length-2])));
                polyline.add(currentSegment);
            }



            color =Integer.parseInt(components[components.length-2]);
                System.out.println("we just set color to " + color);
            id = Integer.parseInt(components[components.length-1]);

        }

        if (components[0].contentEquals("d")){/*delete*/
            action = "d";
            id = Integer.parseInt(components[components.length-1]);
        }

        if (components[0].contentEquals("m")){/*move*/
            action = "m";

            id = Integer.parseInt(components[1]);
            dx = Integer.parseInt(components[2]);
            dy = Integer.parseInt(components[3]);
            /*sets up for a move*/
        }
        if (components[0].contentEquals("r")) { /*recolor*/

            action = "r";
            id = Integer.parseInt(components[1]);
            color = Integer.parseInt(components[2]);


        }
    }}

    public String getAction(){return action;}

    public int getID(){return id;}

    public String getShapeName(){return shapeName;}

    public ArrayList<Segment> getPolyline(){return polyline;}

    public int getx1(){return x1;}
    public int gety1(){return y1;}
    public int getx2(){return x2;}
    public int gety2(){return y2;}

    public int getColor(){return color;}

    public int getdx(){return dx;}
    public int getdy(){return dy;}





}
