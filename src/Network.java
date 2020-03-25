import javafx.util.Pair;

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

	//private static List<Pair<Integer, Node>> nodes = new ArrayList<>();
	private static List<Node> nodes = new ArrayList<>();
	private int round;
	private static int period = 20;
	private Map<Node, String> msgToDeliver; //Integer for the id of the sender and String for the message
	private Map<Integer, List<Node>> electionRounds = new HashMap<>();
	private Map<Integer, Node> failureRounds = new HashMap<>();
	private static String file1;
	private static String file2;

//	public Network() {
//		this.start();
//	}

	/*
		Code to call methods for parsing the input file, initiating the system and producing the log can be added here.
	*/
	public void NetSimulator() throws IOException, InterruptedException {
		msgToDeliver = new HashMap<>();
		parseFile(file1);

		for(Node node: nodes){
			for(Node otherNode: nodes){
				for(int i = 0; i < node.neighbourIds.size(); i++){
					int id = node.neighbourIds.get(i);
					if(otherNode.getNodeId() == id){
						node.addNeighbour(otherNode);
					}
				}
			}
		}

		//make sure the ring is bi-directional
		//the first node in the ring
		Node first = nodes.get(0);

		//the node after the first node
		Node next = nodes.get(1);

		//the last node in the ring
		Node last = nodes.get(nodes.size()-1);

		//the node before the last node
		Node prev = nodes.get(nodes.size()-2);

		if(!first.getNeighbors().contains(last)){
			first.addNeighbour(last);
		}

		if(!first.getNeighbors().contains(next)){
			first.addNeighbour(next);
		}

		if(!last.getNeighbors().contains(first)){
			last.addNeighbour(first);
		}

		if(!last.getNeighbors().contains(prev)){
			last.addNeighbour(prev);
		}


		for(int i = 1; i < nodes.size()-1; i++) {
			Node current = nodes.get(i);
			Node prevNode = nodes.get(i - 1);
			Node nextNode = nodes.get(i + 1);
			if (!current.getNeighbors().contains(prevNode)) {
				current.addNeighbour(prevNode);
			}

			if (!current.getNeighbors().contains(nextNode)) {
				current.addNeighbour(nextNode);
			}

		}

		parseFile(file2);
		round = 0;

		while (!electionRounds.isEmpty() || !msgToDeliver.isEmpty() || !failureRounds.isEmpty()) {
			System.out.println("-----------ROUND: " + round + "----------------");
			if (!electionRounds.isEmpty() && electionRounds.containsKey(round)) {
				for (Node node : electionRounds.get(round)) {
					System.out.println("********" + node.getNodeId() + " wants to start election**************");
					synchronized (this) {
						node.outgoingMsg.add("ELECT " + node.getNodeId());
					}
				}
			}
			addMessages();
			deliverMessages();
			msgToDeliver.clear();
			electionRounds.remove(round);

			if (failureRounds.containsKey(round)) {
				synchronized (this) {
					Node failedNode = failureRounds.get(round);
					int position = nodes.indexOf(failedNode);
					int nextPos;
					int previousPos;
					if(position == nodes.size()-1){
						nextPos = 0;
						previousPos = position - 1;
					}
					else if(position == 0){
						previousPos = nodes.size()-1;
						nextPos = position +1;

					}
					else{
						nextPos = position +1;
						previousPos = position - 1;
					}
					System.out.println("Node " + failedNode.getNodeId() + " has failed in round " + round);
					//inform the node's neighbours remove the node from the network
					informNodeFailure(failedNode);
					if (isNetworkConnected(nodes.get(previousPos), nodes.get(nextPos))) {
						if (failedNode.isNodeLeader()) {
							System.out.println("The failed node" + failedNode + "was the leader unfortunately");
							System.out.println("The neighbouring right-hand node will restart the election in the nextPos round");
							List<Node> startElectionNode = new ArrayList<>();
							startElectionNode.add(nodes.get(nextPos));
							electionRounds.put(round + 1, startElectionNode);
							nodes.remove(failedNode);
						} else {
							System.out.println("The failed node" + failedNode + "was not the leader fortunately");
							nodes.remove(failedNode);
						}
					} else {
						System.out.println("Network is disconnected... terminating program...");
						System.exit(0);
					}
				}
				failureRounds.remove(round);
			}
				Thread.sleep(period);
				round++;
		}

		addMessages();
		while(!msgToDeliver.isEmpty()){
			System.out.println("-----------ROUND: " + round +"----------------");
			deliverMessages();
			msgToDeliver.clear();
			addMessages();
			Thread.sleep(period);
			round++;
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
				String mode = fields[0];
				//elections
				if (mode.equals("ELECT")) {
					List<Node> startElection = new ArrayList<>();
					for (int i = 2; i < fields.length; i++) {
						for(Node node: nodes) {
							if(Integer.parseInt(fields[i]) == node.getNodeId())
								startElection.add(node);
						}
					}
					electionRounds.put(Integer.parseInt(fields[1]), startElection);
				}

				//failures - only a single node fails in a round.
				else if (mode.equals("FAIL")) {
					List<Node> failures = new ArrayList<>();
						for (Node node : nodes) {
							if (Integer.parseInt(fields[2]) == node.getNodeId())
								failureRounds.put(Integer.parseInt(fields[1]), node);
						}

				} else {
					Integer nodeId = Integer.parseInt(mode);
					Node mainNode = new Node(nodeId, this);
					//System.out.println(fields[0]);
					for (int i = 1; i < fields.length; i++) {
						mainNode.neighbourIds.add(Integer.parseInt(fields[i]));
					}
					nodes.add(mainNode);
				}

			}
			// close the reader
			br.close();


		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void addMessages() {
		/*
		At each round, the network collects all the messages that the nodes want to send to their neighbours.
		Implement this logic here.
		*/
		for (int i = 0; i < this.nodes.size(); ++i) {
			List<String> outgoing = ((Node) this.nodes.get(i)).getOutgoingMessages();

			for (int j = 0; j < outgoing.size(); ++j) {
				synchronized (outgoing) {
					msgToDeliver.put((Node) this.nodes.get(i), (String) outgoing.get(j));
					System.out.println("Node" + ((Node) this.nodes.get(i)).getNodeId() + " has sent a message: " + (String) outgoing.get(j));
					outgoing.remove(j);
				}
			}
		}
	}

	public synchronized void deliverMessages() throws InterruptedException, IOException {
		/*
		At each round, the network delivers all the messages that it has collected from the nodes.
		Implement this logic here.
		The network must ensure that a node can send only to its neighbours, one message per round per neighbour.
		*/
		for (Node node : nodes) {
			String message = msgToDeliver.get(node);
			if (message != null) {
				synchronized (this) {
					int currIndex = nodes.indexOf(node) + 1;
					if (currIndex <= nodes.size() - 1)
						nodes.get(currIndex).receiveMsg(message);
					else
						nodes.get(0).receiveMsg(message);
					msgToDeliver.remove(node);
				}
			}
		}
	}

	public synchronized void informNodeFailure ( Node failedNode){
	/*
	Method to inform the neighbours of a failed node about the event.
	*/
		synchronized (this) {
			for (Node n : failedNode.getNeighbors()) {
				n.myNeighbours.remove(failedNode);
			}
		}
	}

	public synchronized boolean isNetworkConnected ( Node leftNode, Node rightNode){
	/*
	Method to check if the network is still connected using BFS
	*/

		LinkedList<Node> queue = new LinkedList<Node>();

		// Mark all the nodes as not visited(By default set
		// as false)
		//boolean visited[] = new boolean[nodes.size()];
		Integer currentNodeId = leftNode.getNodeId();
		Integer	destinationNodeId = rightNode.getNodeId();
		// Mark the current node as visited and enqueue it
		queue.add(leftNode);
		while(!queue.isEmpty()){

			//get the first node in the queue
			Node s = queue.poll();

			if(s.getNeighbors().contains(leftNode)){
				System.out.println("Network is still connected as path found between nodes" + currentNodeId + "and "+ destinationNodeId);
				return true;
			}

			else {
					queue.addAll(s.getNeighbors());
			}
		}
		return false;
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
