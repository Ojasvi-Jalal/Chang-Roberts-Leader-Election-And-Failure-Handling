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

	private static List<Node> nodes = new ArrayList<>();
	private int round;
	private static int period = 20;
	private Map<Integer, String> msgToDeliver; //Integer for the id of the sender and String for the message
	private Map<Integer, List<Node>> electionRounds = new HashMap<>();
	private static String file1;
	private static String file2;

	/*
		Code to call methods for parsing the input file, initiating the system and producing the log can be added here.
	*/
	public void NetSimulator() throws IOException, InterruptedException {
		msgToDeliver = new HashMap<Integer, String>();
		parseFile(file1);
		nodes.forEach(node -> new Thread(node).start());
		parseFile(file2);
		round = 0;
//		while(msgToDeliver.isEmpty() && !electionRounds.isEmpty()) {
//			Thread.sleep(period);
//			round++;
//		}

		while(!electionRounds.isEmpty()) {
			if(electionRounds.containsKey(round)){
				for(Node node: electionRounds.get(round)){
					node.receiveMsg("ELECT "+node.getNodeId());
					node.sendMsg();
				}
				deliverMessages();
				msgToDeliver.clear();
				electionRounds.remove(round);
				Thread.sleep(period);
				round++;
			}
			else{
				round++;
			}
		}
	}

	private void parseFile(String fileName) throws IOException {
   		/*
   		Code to parse the file can be added here. Notice that the method's descriptor must be defined.
   		*/

		try {
			// create a reader instance
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			// read until end of file
			String line;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\\s");
				if (fields[0].equals("ELECT")){
					List<Node> startElection = new ArrayList<>();
					for(int i = 2; i < fields.length; i++){
						startElection.add(new Node(Integer.parseInt(fields[i]), this));
					}
					electionRounds.put(Integer.parseInt(fields[1]), startElection);
				}

				else if (fields[0].equals("FAIL")){

				}

				else {
					Node mainNode = new Node(Integer.parseInt(fields[0]), this);
					//System.out.println(fields[0]);
					for (int i = 1; i < fields.length; i++) {
						mainNode.addNeighbour(new Node(Integer.parseInt(fields[i]), this));
					}
					nodes.add(mainNode);
				}

			}
			// close the reader
			br.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

//check nodes and neighbours created alright
//        for(Node node: nodes){
//            System.out.print(node.getNodeId()+" ");
//            for(Node neighbours : node.myNeighbours){
//                System.out.print(neighbours.getNodeId()+",");
//            }
//            System.out.println();
//        }

        //elections
//		System.out.print("*********Round********");
//		for(Integer round: electionRounds.keySet()){
//			System.out.print(round+" ");
//			for(Node othernodes : electionRounds.get(round)){
//				System.out.print(othernodes.getNodeId()+",");
//			}
//			System.out.println();
//		}
	}

	public synchronized void addMessage(int id, String m) {
		/*
		At each round, the network collects all the messages that the nodes want to send to their neighbours.
		Implement this logic here.
		*/
		msgToDeliver.put(id, m);
	}

	public synchronized void deliverMessages() throws InterruptedException {
		/*
		At each round, the network delivers all the messages that it has collected from the nodes.
		Implement this logic here.
		The network must ensure that a node can send only to its neighbours, one message per round per neighbour.
		*/
		for(Integer senderId: msgToDeliver.keySet()) {
			nodes.get(nodes.indexOf(new Node(senderId, this))+1).receiveMsg(msgToDeliver.get(senderId));
		}

	}

	public synchronized void informNodeFailure(int id) {
		/*
		Method to inform the neighbours of a failed node about the event.
		*/
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		/*
		Your main must get the input file as input.
		*/
		Network network = new Network();
		network.file1 = args[0];
		network.file2 = args[1];
		network.NetSimulator();
	}
}
