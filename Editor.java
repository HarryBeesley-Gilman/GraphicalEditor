
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Harry Beesley-Gilman on Scaffold code, 5/1/22
 * purpose: editer program to allow you to build shapes, send messages, recieve and understand messages (to a degree)
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int tempId = 0;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point drawTo = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged

	private MessageParser messageParser;
	private int maxID = 0;					// current shape id (if any; else -1) being moved

	private boolean activated; /*whether illustration has been caught up */



	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		activated = false;

		messageParser = new MessageParser();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease(event.getPoint());
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}


	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {

		for (Shape shapey : sketch.getShapes().values()){
			shapey.draw(g);
		}
		if (curr!=null) {
			curr.draw(g);
		}
	}


	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		drawFrom = p; /*updates every time you press*/


		if (mode == Mode.DRAW) {
			// Start a new shape
			if (shapeType.equals("ellipse")) {
				curr = new Ellipse((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY(), color);
			}
			else if (shapeType.equals("rectangle")) {
				curr = new Rectangle((int) p.getX(), (int) p.getY(), color);
			}
			else if (shapeType.equals("freehand")) {
				curr = new Polyline((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY(), color);
			}
			else if (shapeType.equals("segment")) {
				curr = new Segment(p.x, p.y, color);
			}
		}

		else if (mode== Mode.RECOLOR) {
			for (Shape shapey: sketch.getShapes().values()){
				if (shapey.contains((int)p.getX(),(int)p.getY())){
					curr=shapey;
					shapey.setColor(color);
				}
				/*set current and go ahead and change color in shapey*/
			}

			if (curr!=null){
				if (curr.contains((int)p.getX(), (int)p.getY())){curr.setColor(color);}}
		}

		else if (mode==Mode.MOVE) { /*allows you to set new curr where you click*/
			curr = sketch.findShape(p);
			moveFrom = drawFrom;
			for (Shape shapey : sketch.getShapes().values()) {
				if (shapey.contains((int) p.getX(), (int) p.getY())) {
					curr = shapey;
					break;
				}
			}
		}
		drawFrom=p;
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p){

		tempId=sketch.getID(curr);

		if (mode == mode.MOVE && curr!=null){ /*move by the difference between the current mouse location and where the shape was
		last time the function updated, tiny, streamlined increments*/
			curr.moveBy((int)(p.getX()-moveFrom.getX()),(int)(p.getY()-moveFrom.getY()));
			moveFrom=p;
		}
		/*change the corners of the shape*/
		else if (mode==Mode.DRAW){
			if (shapeType.contentEquals("ellipse")){
				curr.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			}
			else if (shapeType.contentEquals("rectangle")){
				curr.setCorners(drawFrom.x, drawFrom.y, (int)p.getX(), (int)p.getY());
			}

			else if (shapeType.contentEquals("freehand")){
				curr.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
				drawFrom=p;
			}
			else if(shapeType.equals("segment"))
				((Segment)curr).setEnd(p.x, p.y);
		}
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease(Point p) {

		String message = "";

		if (mode==Mode.MOVE){
			/*move the current shape*/
			curr.moveBy((int)((-1)*(p.getX()-drawFrom.getX())),(int)(-1*(p.getY()-drawFrom.getY())));

			curr=null;
			/*prepare message to send out, shape already exists in other clients so we just ned to tell it how to move*/
			message= message.concat("m,");
			message=message.concat(tempId+",");
			message=message.concat((int)(p.getX()-drawFrom.getX()) + "," + (int)(p.getY() - drawFrom.getY()));
			/*ID needs to be reset for the next shape*/
			tempId=-1;
		}

		/*send out message saying what shape to build*/
		else if (mode==Mode.DRAW) {
			message= message.concat("a,");
			message = message.concat(curr.toString());
			while (sketch.getShapes().keySet().contains(maxID)){
				maxID+=1;
			}
			message = message.concat(Integer.toString(maxID));
			/*message for a polyline is different*/
			if (curr.getClass().toString()==("Polyline")|| curr.getClass().toString()==("polyline")){
				message=message.concat(Integer.toString(messageParser.getColor()));
			}

			curr=null;
		}

		/*sned out recolor message with Id and color*/
		else if (mode==Mode.RECOLOR) {
			if (curr!=null) {
				message=message.concat("r,");
				tempId = sketch.getID(curr);
				message = message.concat(Integer.toString(tempId));
				message=message.concat(","+ color.getRGB());

			}
		}

		/*only need delete and Id instructions needed*/
		else if (mode==Mode.DELETE) {
			message = message.concat("d,");

			curr = sketch.findShape(p);

			message = message.concat(Integer.toString(sketch.getID(curr)));
			sketch.getShapes().remove(sketch.getID(curr));

		}
		comm.send(message);
		moveFrom = null;
		curr=null; /*let's not keep illustrating current now that we've sent our request -- It's a duplicate of the
		bounceback we'll recieve*/
	}

	public boolean getActivated(){return activated==true;}

	public void setAtivated(){activated=true;}


	/*this method takes a request (in the form of a String), parses it, does the requisite action -- This can mean
	creating a new shape and adding it to the local sketch. Also handles recolors, moves, and deletions.*/
	public void draw(String currentLine){


		messageParser.parse(currentLine);

		/*create the shape and add them to our local sketch.*/
		if (messageParser.getAction().contentEquals("a")){
			if (messageParser.getShapeName().contentEquals("rectangle")){

				Shape newShape = new Rectangle(messageParser.getx1(),messageParser.gety1(),messageParser.getx2(),
						messageParser.gety2(), new Color(messageParser.getColor()), messageParser.getID());
				sketch.addShape(messageParser.getID(), newShape);
			}

			if (messageParser.getShapeName().contentEquals("segment")){
				Shape newShape = new Segment(messageParser.getx1(),messageParser.gety1(),messageParser.getx2(),
						messageParser.gety2(), new Color(messageParser.getColor()), messageParser.getID());
				sketch.addShape(messageParser.getID(), newShape);

			}

			if (messageParser.getShapeName().contentEquals("ellipse")){
				Shape newShape = new Ellipse(messageParser.getx1(),messageParser.gety1(),messageParser.getx2(),
						messageParser.gety2(), new Color(messageParser.getColor()), messageParser.getID());
				sketch.addShape(messageParser.getID(), newShape);
			}

			if (messageParser.getShapeName().contentEquals("polyline")){
				Polyline newShape = new Polyline(new Color(messageParser.getColor()), messageParser.getID());
				newShape.setColor(new Color(messageParser.getColor()));
				/*go through all segments and add them.*/
				for (Segment s : messageParser.getPolyline()){
					s.setColor(new Color(messageParser.getColor()));
					newShape.addSegment(s);
				}
				sketch.addShape(messageParser.getID(), newShape);
			}
		}

		if (messageParser.getAction().contentEquals("d")){
			sketch.removeShape(messageParser.getID());
		}
		/*recolor current and send instruction out*/
		if (messageParser.getAction().contentEquals("r")){

			if (curr!=null){
				curr.setColor(new Color(messageParser.getColor()));
			}

			sketch.recolor(messageParser.getID(), new Color(messageParser.getColor()));
			repaint();
		}

		if (messageParser.getAction().contentEquals("m")){
			sketch.move(messageParser.getID(),messageParser.getdx(), messageParser.getdy());
			repaint();
		}

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}

		});
	}
}