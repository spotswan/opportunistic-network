/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.HashMap;


import routing.MessageRouter;
//import core.SimClock;

/**
 * A connection between two DTN nodes.
 */
public abstract class Connection {
	protected DTNHost toNode;
	protected NetworkInterface toInterface;
	protected DTNHost fromNode;
	protected NetworkInterface fromInterface;
	protected DTNHost msgFromNode;

	public boolean isUp; //changed from protected to public S.Saha to not send messages to black hole node
	protected Message msgOnFly;
	/** how many bytes this connection has transferred */
	protected int bytesTransferred;


	public int up;
	public int low;
	/**
	 * Creates a new connection between nodes and sets the connection
	 * state to "up".
	 * @param fromNode The node that initiated the connection
	 * @param fromInterface The interface that initiated the connection
	 * @param toNode The node in the other side of the connection
	 * @param toInterface The interface in the other side of the connection
	 */
	public Connection(DTNHost fromNode, NetworkInterface fromInterface, 
			DTNHost toNode, NetworkInterface toInterface) {
		
		this.fromNode = fromNode;
		this.fromInterface = fromInterface;
		this.toNode = toNode;
		this.toInterface = toInterface;
		this.isUp = true;
		this.bytesTransferred = 0;
		
//		if(fromNode.getAddress() == 10 || fromNode.getAddress() == 20 || fromNode.getAddress() == 30 || fromNode.getAddress() == 40) {
//			this.isUp = false;
//
//		}
//		if(toNode.getAddress() == 10 || toNode.getAddress() == 20 || toNode.getAddress() == 30 || toNode.getAddress() == 40) {
//			this.isUp = false;
//		}
		
		//**MANUAL ENTRY OF NUMBER OF NODES** **HC**
		int nrofHosts = fromNode.nrofNodes;
		int police = fromNode.police;
		int lowergh = fromNode.lowergh;
		int uppergh = fromNode.uppergh;
		up = uppergh;
		low = lowergh;
		//System.out.println(fromNode.getAddress() + " " + toNode.getAddress());
		int from = fromNode.getAddress();
		int to = toNode.getAddress();

		//If both are police Nodes
		//**HC**
		if(from < police && to < police) {
			//HashMap<Integer, int[]> fromH = new HashMap<Integer, int[]>();

			for(int i = 0; i < nrofHosts; i++) {
				HashMap<Integer, int[]> fromH = fromNode.policeMap.get(i);
				HashMap<Integer, int[]> toH = toNode.policeMap.get(i);
				if(fromH.isEmpty() && toH.isEmpty()) continue;
				else{
					
					if(toH.isEmpty()) {
						toH = fromH;
						toNode.time[i] = fromNode.time[i];
						if(i >= lowergh && i <= uppergh) //hack *HC*
						check(i, fromH, toNode);
					}
					else if(fromH.isEmpty()) {
						fromH = toH;
						fromNode.time[i] = toNode.time[i];
						if(i >= lowergh && i <= uppergh) //hack *HC*
						check(i, toH, fromNode);
					}
					else {
						if(fromNode.time[i] > toNode.time[i]) {
							toH = fromH;
							toNode.time[i] = fromNode.time[i];
							if(i >= lowergh && i <= uppergh) //hack *HC*
							check(i, fromH, toNode);
						}
						else {
							fromH = toH;
							fromNode.time[i] = toNode.time[i];
							if(i >= lowergh && i <= uppergh) //hack *HC*
							check(i, toH, fromNode);
						}
					}
				}
				
			}
			for(int i = 0; i < nrofHosts; i++) {
				int max = toNode.gH[i] > fromNode.gH[i] ? toNode.gH[i] : fromNode.gH[i];
				toNode.gH[i] = max;
				fromNode.gH[i] = max;
			}
			
		}
		//If from is Police Node
		//**HC**
		if(from < police) {
			HashMap<Integer, int[]> toCopy = new HashMap<Integer, int[]>();
			toCopy = toNode.mesTran;
			fromNode.policeMap.add(to, toCopy);
			fromNode.time[to] = SimClock.getIntTime();
			//System.out.println("I am here" + SimClock.getIntTime());
			if((!toCopy.isEmpty()) && to>=lowergh && to<=uppergh) { //**HC*
				check(to, toCopy, fromNode);
			}
			//Sending the greyHole values
			for(int i = 0; i < nrofHosts; i++) {
				int max = toNode.gH[i] > fromNode.gH[i] ? toNode.gH[i] : fromNode.gH[i];
				toNode.gH[i] = max;
				fromNode.gH[i] = max; //accepting Grey Hole values from Nodes
			}
			
		}
		//If To is Police Node
		if(to < police) {
			HashMap<Integer, int[]> toCopy = new HashMap<Integer, int[]>();
			toCopy = fromNode.mesTran;
			toNode.policeMap.add(from, toCopy);
			toNode.time[from] = SimClock.getIntTime();
			
			
			if((!toCopy.isEmpty()) && from>=lowergh && from<=uppergh) { //**HC**
				check(from, toCopy, toNode);
			}
			//Sending the greyHole values
			for(int i = 0; i < nrofHosts; i++) {
				int max = toNode.gH[i] > fromNode.gH[i] ? toNode.gH[i] : fromNode.gH[i];
				toNode.gH[i] = max; //accepting Grey Hole values from Nodes
				fromNode.gH[i] = max;
			}
		}
		

	}


	/**
	 * Returns true if the connection is up
	 * @return state of the connection
	 */
	public boolean isUp() {
		return this.isUp;
	}
	
	public void check(int n, HashMap<Integer, int[]> h, DTNHost host) {
		for(int it: h.keySet()) {
			int arr[] = new int[2];
			arr = h.get(it);
			//if(up == 0 || low == 0) System.out.println("Mistake");
			if(arr[0]>=45 || arr[0] <=50) { //**HC** up low
				HashMap<Integer, int[]> match = host.policeMap.get(arr[0]);
			if(!match.isEmpty())
					host.gH[n] = 1; 
			}
		}
	}
	/** 
	 * Returns true if the connections is transferring a message
	 * @return true if the connections is transferring a message
	 */
	public boolean isTransferring() {
		return this.msgOnFly != null;
	}

	
	/**
	 * Returns true if the given node is the initiator of the connection, false
	 * otherwise
	 * @param node The node to check
	 * @return true if the given node is the initiator of the connection
	 */
	public boolean isInitiator(DTNHost node) {
		return node == this.fromNode;
	}
	
	/**
	 * Sets the state of the connection.
	 * @param state True if the connection is up, false if not
	 */
	public void setUpState(boolean state) {
		this.isUp = state;
	}

	/**
	 * Sets a message that this connection is currently transferring. If message
	 * passing is controlled by external events, this method is not needed
	 * (but then e.g. {@link #finalizeTransfer()} and 
	 * {@link #isMessageTransferred()} will not work either). Only a one message
	 * at a time can be transferred using one connection.
	 * @param m The message
	 * @return The value returned by 
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public abstract int startTransfer(DTNHost from, Message m);

	/**
	 * Calculate the current transmission speed from the information
	 * given by the interfaces, and calculate the missing data amount.
	 */
	public void update() {};

	/**
     * Aborts the transfer of the currently transferred message.
     */
	public void abortTransfer() {
		assert msgOnFly != null : "No message to abort at " + msgFromNode;	
		int bytesRemaining = getRemainingByteCount();

		this.bytesTransferred += msgOnFly.getSize() - bytesRemaining;

		getOtherNode(msgFromNode).messageAborted(this.msgOnFly.getId(),
				msgFromNode, bytesRemaining);
		clearMsgOnFly();
	}	

	/**
	 * Returns the amount of bytes to be transferred before ongoing transfer
	 * is ready or 0 if there's no ongoing transfer or it has finished
	 * already
	 * @return the amount of bytes to be transferred
	 */
	public abstract int getRemainingByteCount();

	/**
	 * Clears the message that is currently being transferred.
	 * Calls to {@link #getMessage()} will return null after this.
	 */
	protected void clearMsgOnFly() {
		this.msgOnFly = null;
		this.msgFromNode = null;		
	}

	/**
	 * Finalizes the transfer of the currently transferred message.
	 * The message that was being transferred can <STRONG>not</STRONG> be
	 * retrieved from this connections after calling this method (using
	 * {@link #getMessage()}).
	 */
	public void finalizeTransfer() {
		assert this.msgOnFly != null : "Nothing to finalize in " + this;
		assert msgFromNode != null : "msgFromNode is not set";
		
		this.bytesTransferred += msgOnFly.getSize();
		//System.out.println("In connection.java");
		getOtherNode(msgFromNode).messageTransferred(this.msgOnFly.getId(),
				msgFromNode);
		clearMsgOnFly();
	}

	/**
	 * Returns true if the current message transfer is done 
	 * @return True if the transfer is done, false if not
	 */
	public abstract boolean isMessageTransferred();

	/**
	 * Returns true if the connection is ready to transfer a message (connection
	 * is up and there is no message being transferred).
	 * @return true if the connection is ready to transfer a message
	 */
	public boolean isReadyForTransfer() {
		return this.isUp && this.msgOnFly == null; 
	}

	/**
	 * Gets the message that this connection is currently transferring.
	 * @return The message or null if no message is being transferred
	 */	
	public Message getMessage() {
		return this.msgOnFly;
	}

	/** 
	 * Gets the current connection speed
	 */
	public abstract double getSpeed();	

	/**
	 * Returns the total amount of bytes this connection has transferred so far
	 * (including all transfers).
	 */
	public int getTotalBytesTransferred() {
		if (this.msgOnFly == null) {
			return this.bytesTransferred;
		}
		else {
			if (isMessageTransferred()) {
				return this.bytesTransferred + this.msgOnFly.getSize();
			}
			else {
				return this.bytesTransferred + 
				(msgOnFly.getSize() - getRemainingByteCount());
			}
		}
	}

	/**
	 * Returns the node in the other end of the connection
	 * @param node The node in this end of the connection
	 * @return The requested node
	 */
	public DTNHost getOtherNode(DTNHost node) {
		//System.out.println(toNode);
		if (node == this.fromNode) {
			return this.toNode;
		}
		else {
			return this.fromNode;
		}
	}

	/**
	 * Returns the interface in the other end of the connection
	 * @param i The interface in this end of the connection
	 * @return The requested interface
	 */
	public NetworkInterface getOtherInterface(NetworkInterface i) {
		if (i == this.fromInterface) {
			return this.toInterface;
		}
		else {
			return this.fromInterface;
		}
	}

	/**
	 * Returns a String presentation of the connection.
	 */
	public String toString() {
		//System.out.println(fromNode + "<->" + toNode);
		return fromNode + "<->" + toNode + " (" + getSpeed()/1000 + " kBps) is " +
		(isUp() ? "up":"down") + 
		(isTransferring() ? " transferring " + this.msgOnFly  + 
				" from " + this.msgFromNode : "");
	}

}

