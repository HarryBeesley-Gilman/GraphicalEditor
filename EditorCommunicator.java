import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Harry Beesley-Gilman on Scaffold. 5/1/22
 * @purpose code editor communicator to serve as condtact between editors and server communicators
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;        // to server
	private BufferedReader in;        // from server
	protected Editor editor;        // handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		} catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		System.out.println("editorcommunicator "+ msg);
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages

			//gets from server --> prints to editor
			String currentLine;
			while((currentLine = in.readLine()) != null) {
				if (currentLine.length()>0){
					/*special protocal for if we're being updated on the state of the world at the creation of a new
					* editor. First line will contain "start broadcasting" for in this scenario.*/
					if (currentLine.contains("start broadcasting")) {
						int toGo = Integer.parseInt(in.readLine());
						for (int i = 0; i < toGo; i++) {
							currentLine = in.readLine();
							if (!editor.getActivated()) {
								editor.draw("a,"+currentLine); /*wont wrk*/
								editor.setAtivated();
							}
						}
					}
					/*most frequent scenario. Run draw method on the line that has been fed in.*/
					else {
						editor.draw(currentLine);
						editor.repaint();}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("server hung up");
		}
	}
}
