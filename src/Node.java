import java.util.*;

/* Class to represent a node. Each node must run on its own thread.*/

public class Node extends Thread {

	private int currentNodeId;
	private int leaderId = Integer.MIN_VALUE;
	private boolean participant = false;
	private boolean isLeader = false;
	private Network network;
	
	// Neighbouring nodes
	public List<Node> myNeighbours;

	// Queues for the incoming messages
	public List<String> incomingMsg;

	// Queues for the outgoing messages
	public List<String> outgoingMsg = new ArrayList<>();
	
	public Node(int id, Network network){
	
		this.currentNodeId = id;
		this.network = network;
		
		myNeighbours = new ArrayList<Node>();
		incomingMsg = new ArrayList<String>();
		this.start();
	}
	
	// Basic methods for the Node class
	
	public int getNodeId() {
		/*
		Method to get the Id of a node instance
		*/
		return currentNodeId;
	}
			
	public boolean isNodeLeader() {
		/*
		Method to return true if the node is currently a leader
		*/
		return isLeader;
	}
		
	public List<Node> getNeighbors() {
		/*
		Method to get the neighbours of the node
		*/
		return myNeighbours;
	}

	public List<String> getOutgoingMessages() {
		/*
		Method to get the neighbours of the node
		*/
		return outgoingMsg;
	}
		
	public void addNeighbour(Node n) {
		/*
		Method to add a neighbour to a node
		*/
		myNeighbours.add(n);
	}
				
	public void receiveMsg(String m) {
		/*
		Method that implements the reception of an incoming message by a node
		*/
		String start		= m.split(" ")[0];
		int senderId		= Integer.parseInt(m.split(" ")[1]);

		if(start.equals("ELECT")) {
			// the uid in the election message is larger, so the current process just forwards that message
			if (senderId > currentNodeId) {
				outgoingMsg.add(m);
			}

			// the uid in the message is smaller and the current process is not yet a participant
			else if (currentNodeId > senderId && participant == false) {
				participant = true;
				outgoingMsg.add("ELECT "+ currentNodeId);
			}

			else if (currentNodeId == senderId && participant == false) {
				participant = true;
				outgoingMsg.add("ELECT "+ currentNodeId);
			}
			// the current process starts acting as leader
			else if (currentNodeId == senderId && participant == true) {
				isLeader = true;
				leaderId = currentNodeId;
				System.out.println("FIRST STAGE:The new leader is process " + currentNodeId);
				outgoingMsg.add("LEADER "+ currentNodeId);
			}
		}

		else if(start.equals("LEADER")) {
			if (senderId != currentNodeId) {
				participant = false;
				leaderId = senderId;
				outgoingMsg.add(m);
				System.out.println("SECOND STAGE: The new leader is process " + currentNodeId);
			}
		}

//		if(outgoingMsg.size()> 0)
//			sendMsg();
	}

	public void startElection(){

	}
		
	public void sendMsg() {
		/*
		Method that implements the sending of a message by a node. 
		The message must be delivered to its recepients through the network.
		This method need only implement the logic of the network receiving an outgoing message from a node.
		The remainder of the logic will be implemented in the network class.
		*/
//		if(outgoingMsg.size()> 0) {
//			for (int i = 0; i < outgoingMsg.size(); i++)
//				network.addMessage(currentNodeId, outgoingMsg.get(i));
//		}
//		outgoingMsg.clear();
	}
	
}
