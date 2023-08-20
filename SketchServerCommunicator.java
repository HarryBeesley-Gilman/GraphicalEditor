import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;                    // to talk with client
	private BufferedReader in;                // from client
	private PrintWriter out;                // to client
	private SketchServer server;            // handling communication for
	private MessageParser parser;


	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
		parser = new MessageParser();

	}

	/**
	 * Sends a message to the client
	 *
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			System.out.println("start broadcasting ");
			for (Shape s : server.getGlobalMap().getShapes().values()){
				server.broadcast("a," + s.toString()+ server.getGlobalMap().getID(s));
			}
			// Keep getting and handling messages from the client
			String currentLine = in.readLine();
			//hold id to add at the end
			int id;

			while (currentLine != null) {//multiple clients
				parser.parse(currentLine);
				id = parser.getID();
				/*Adds shapes to the global sketch based on parsed line and prepare a message to send out.*/
				if (parser.getAction().contentEquals("a") && parser.getShapeName()!=null) {
					if (parser.getShapeName().contentEquals("rectangle")) {
						Shape newShape = new Rectangle(parser.getx1(), parser.gety1(), parser.getx2(),
								parser.gety2(), new Color(parser.getColor()), parser.getID());
						server.getGlobalMap().addShape(parser.getID(), newShape);
						currentLine = "a,r," + parser.getx1() + "," + parser.gety1() + "," + parser.getx2() + "," + parser.gety2() + "," + parser.getColor() + "," + id;

					}

					if (parser.getShapeName().contentEquals("segment")) {
						Shape newShape = new Segment(parser.getx1(), parser.gety1(), parser.getx2(),
								parser.gety2(), new Color(parser.getColor()), parser.getID());
						server.getGlobalMap().addShape(parser.getID(), newShape);
						currentLine = "a,s," + parser.getx1() + "," + parser.gety1() + "," + parser.getx2() + "," + parser.gety2() + "," + parser.getColor() + "," + id;

					}

					if (parser.getShapeName().contentEquals("ellipse")) {
						Shape newShape = new Ellipse(parser.getx1(), parser.gety1(), parser.getx2(),
								parser.gety2(), new Color(parser.getColor()), parser.getID());
						server.getGlobalMap().addShape(parser.getID(), newShape);
						currentLine = "a,e," + parser.getx1() + "," + parser.gety1() + "," + parser.getx2() + "," +
								parser.gety2() + "," + parser.getColor() + "," + id;

					}

					if (parser.getShapeName().contentEquals("polyline")) {
						Polyline newShape = new Polyline(new Color(parser.getColor()), parser.getID());
						currentLine = "a,p,";
						for (Segment seg : parser.getPolyline()) {    //for each segment in the polyline message - we add it to the polyline
							newShape.addSegment(seg);

							currentLine = currentLine.concat(seg.getCornerList()+",");
						}
						currentLine = currentLine.concat(Integer.toString(parser.getColor()));
						currentLine= currentLine.concat("," + id);
						server.getGlobalMap().addShape(parser.getID(), newShape);
					}
				}

				if (parser.getAction().contentEquals("d")) {
					currentLine = "d," + parser.getID();
					server.getGlobalMap().removeShape(parser.getID());
				}

				if (parser.getAction().contentEquals("r")) {
					currentLine = "r," + parser.getID() + "," + parser.getColor();

					server.getGlobalMap().recolor(parser.getID(), new Color(parser.getColor()));

				}

				if (parser.getAction().contentEquals("m")) {
					currentLine = "m," + parser.getID() + "," + parser.getdx() + "," + parser.getdy();
					server.getGlobalMap().move(parser.getID(), parser.getdx(), parser.getdy());
				}
				server.broadcast(currentLine);
				currentLine = in.readLine();
			}


			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}