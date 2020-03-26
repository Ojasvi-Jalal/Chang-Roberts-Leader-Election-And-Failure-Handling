import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/* Class to represent a node. Each node must run on its own thread.*/

public class Node extends Thread {

	private int currentNodeId;
	private int leaderId = Integer.MIN_VALUE;
	private boolean participant = false;
	private boolean isLeader = false;
	private Network network;
	private boolean whichPartWritten = false;
	
	// Neighbouring nodes
	public List<Node> myNeighbours;

    // Neighbouring node ids
    public List<Integer> neighbourIds;

	// Queues for the incoming messages
	public List<String> incomingMsg;

	// Queues for the outgoing messages
	public List<String> outgoingMsg = new ArrayList<>();

	//class constructor, initialises the node and starts its thread
	public Node(int id, Network network){
	
		this.currentNodeId = id;
		this.network = network;
		
		myNeighbours = new ArrayList<>();
		neighbourIds = new ArrayList<>();
		incomingMsg = new ArrayList<>();
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

	public synchronized void receiveMsg(String m) {
		/*
		Method that implements the reception of an incoming message by a node
		*/
		String start		= m.split(" ")[0];
		int senderId		= Integer.parseInt(m.split(" ")[1]);

		synchronized (this) {
		    //when the election process is still going on
            if (start.equals("ELECT") || start.equals("FORWARD")) {
                // the uid in the election message is larger, so the current process just forwards that message
                if (senderId > currentNodeId) {
                    outgoingMsg.add(m);
                    if (participant == false)
                        participant = true;
                }

                // the uid in the message is smaller and the current process is not yet a participant
                else if (currentNodeId > senderId && participant == false) {
                    participant = true;
                    outgoingMsg.add("FORWARD " + currentNodeId);
                }

                //case when the node itself starts the election
                else if (currentNodeId == senderId && participant == false) {
                    participant = true;
                    outgoingMsg.add("FORWARD " + currentNodeId);
                }

                // the current process starts acting as leader
                else if (currentNodeId == senderId && participant == true) {
                    participant = false;
                    isLeader = true;
                    leaderId = currentNodeId;
                    System.out.println("FIRST STAGE:The new leader is process " + currentNodeId);
                    outgoingMsg.add("LEADER " + currentNodeId);

                    try {
                        FileHandler handler = new FileHandler("log.txt", true);
                        Logger logger = Logger.getLogger("com.javacodegeeks.snippets.core");
                        logger.addHandler(handler);
                        if(!whichPartWritten)
                            logger.finer(this.network.part+"\n");
                        logger.finer("Leader Node " + leaderId + "\n");
                        System.out.println("Successfully logged to the file.");
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }
            } //when the leader has been elected
            else if (start.equals("LEADER")) {
                if (senderId != currentNodeId) {
                    participant = false;
                    leaderId = senderId;
                    outgoingMsg.add(m);
                    System.out.println("SECOND STAGE: The new leader is process " + leaderId);
                }
                if (senderId == currentNodeId) {
                    System.out.println("SECOND STAGE: Election is over, new leader is " + leaderId);
                }
            }
        }
	}
	
}
