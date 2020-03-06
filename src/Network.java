import java.util.*;
import java.io.*;

/* 
Class to simulate the network. System design directions:

- Synchronous communication: each round lasts for 20ms
- At each round the network receives the messages that the nodes want to send and delivers them
- The network should make sure that:
	- A node can only send messages to its neighbours
	- A node can only send one message per neighbour per round
- When a node fails, the network must inform all the node's neighbours about the failure
*/

public class Network {

	private static List<Node> nodes;
	private int round;
	private int period = 20;
	private Map<Integer, String> msgToDeliver; //Integer for the id of the sender and String for the message

	public Network() {
		msgToDeliver = new HashMap<Integer, String>();
        	
        	/*
        	Code to call methods for parsing the input file, initiating the system and producing the log can be added here.
        	*/
	}

	private static void parseFile(String fileName) throws IOException {
   		/*
   		Code to parse the file can be added here. Notice that the method's descriptor must be defined.
   		*/

		try {
			// create a reader instance
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			// read until end of file
			String line;
			while ((line = br.readLine()) != null) {
				String[] mainNodeAndNeighbours = line.split("\\s");
				Node mainNode = new Node(Integer.parseInt(mainNodeAndNeighbours[0]));
				System.out.println(mainNodeAndNeighbours[0]);
				for(int i = 1; i < mainNodeAndNeighbours.length; i++) {
					mainNode.addNeighbour(new Node(Integer.parseInt(mainNodeAndNeighbours[i])));
				}
				mainNode.addNeighbour(mainNode);
			}
			// close the reader
			br.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		nodes.forEach(node -> new Thread(node).start());
	}

	public synchronized void addMessage(int id, String m) {
		/*
		At each round, the network collects all the messages that the nodes want to send to their neighbours.
		Implement this logic here.
		*/
	}

	public synchronized void deliverMessages() {
		/*
		At each round, the network delivers all the messages that it has collected from the nodes.
		Implement this logic here.
		The network must ensure that a node can send only to its neighbours, one message per round per neighbour.
		*/
	}

	public synchronized void informNodeFailure(int id) {
		/*
		Method to inform the neighbours of a failed node about the event.
		*/
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		/*
		Your main must get the input file as input.
//		*/
//		List<String> lines = FileUtils.readLines(file);
//		lines.get(150);
//		Scanner graph = new Scanner(new FileReader(args[0]));
		parseFile(args[0]);
	}
}
