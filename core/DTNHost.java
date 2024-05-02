/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import movement.MovementModel;
import movement.Path;
import routing.MessageRouter;
import routing.util.RoutingInfo;

//import static core.Constants.DEBUG;
import java.util.Arrays;
import java.util.ArrayList;


import java.util.HashMap;

import java.util.List;

import java.util.Map;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {
	private static int nextAddress = 0;
	private int address;

	private Coord location; 	// where is the host
	private Coord destination;	// where is it going

	private MessageRouter router;
	private MovementModel movement;
	private Path path;
	private double speed;
	private double nextTimeToMove;
	private String name;
	private List<MessageListener> msgListeners;
	private List<MovementListener> movListeners;
	private List<NetworkInterface> net;
	private ModuleCommunicationBus comBus;
    private int[] hmTrustVal;
	private String[] friendList;
	private int noofDropped;
	//private String[] storedFriendList;
    //private Map<String, List<String>> map;
	// ***************  SMK 19th Aug 2019 *****************
	int upto;
	static int i = 0;
	//static int x = 6020; //(for 50 nodes) 
	//static int x = 10000; //(for 50 nodes & 3 bad nodes) 
	//static int x = 40000; //(for 50+ nodes & 3 bad nodes)
	//static int x = 85000;  // 55000 for 125+ nodes 
	static int x = 200000;  //  for 200 nodes 
	//static int x = 2020; //(for 25 nodes and 2 bad nodes)
	private static String [][] rcv_msg = new String[x][4];
	// ***************  SMK 19th Aug 2019 ******************
	
	static {
		DTNSim.registerForReset(DTNHost.class.getCanonicalName());
		reset();
	}
	
	public DTNHost(){
		
	}
	/**
	 * Creates a new DTNHost.
	 * @param msgLs Message listeners
	 * @param movLs Movement listeners
	 * @param groupId GroupID of this host
	 * @param interf List of NetworkInterfaces for the class
	 * @param comBus Module communication bus object
	 * @param mmProto Prototype of the movement model of this host
	 * @param mRouterProto Prototype of the message router of this host
	 */
	public DTNHost(List<MessageListener> msgLs,
			List<MovementListener> movLs,
			String groupId, List<NetworkInterface> interf,
			ModuleCommunicationBus comBus,
			MovementModel mmProto, MessageRouter mRouterProto) {
		this.comBus = comBus;
		this.location = new Coord(0,0);
		this.address = getNextAddress();
		this.name = groupId+address;
		this.net = new ArrayList<NetworkInterface>();

		for (NetworkInterface i : interf) {
			NetworkInterface ni = i.replicate();
			ni.setHost(this);
			net.add(ni);
		}

		// TODO - think about the names of the interfaces and the nodes
		//this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();

		this.msgListeners = msgLs;
		this.movListeners = movLs;

		// create instances by replicating the prototypes
		this.movement = mmProto.replicate();
		this.movement.setComBus(comBus);
		this.movement.setHost(this);
		setRouter(mRouterProto.replicate());

		this.location = movement.getInitialLocation();

		this.nextTimeToMove = movement.nextPathAvailable();
		this.path = null;

		if (movLs != null) { // inform movement listeners about the location
			for (MovementListener l : movLs) {
				l.initialLocation(this, this.location);
			}
		}
     this.hmTrustVal=new int[200];
     this.friendList=new String[200];
	 //this.storedFriendList=new String[50];
	 //this.map=new HashMap<String, List<String>>();
     this.upto=0;	 
	 this.noofDropped=0;
	
	/*try{
		 File file = new File((Paths.get("reports//Friendlist.txt")).toString());
		 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		 String line = null;
		 while((line=br.readLine())!=null)
		 {
			 String[] tokens= line.split("\\s+");
			 if(!(this.toString()).equalsIgnoreCase(tokens[0]))
			 {
		        List<String> valSet = new ArrayList<String>();
				for(int i=1; i< tokens.length; i++)
				{
					valSet.add(tokens[i]);
				}
				map.put(tokens[0], valSet);
		     }
			 else
			 {	 
			 //System.out.print("\n" + this.toString() + " friends are " );
			 int i;
			 for( i=1; i< tokens.length; i++){
				 storedFriendList[i-1]=tokens[i];
			     //System.out.print(tokens[i] + " ");
			 }
			 storedFriendList[i-1]= "\0";
			 //System.out.println();
			 }		 
			 
		 }
	 }*/
		 /*catch(IOException e)
		 {}*/
		 
	 
	}
	
	public DTNHost(String name)
	{
		this.name=name;
	}
	
	/**************  SMK 27 Apr 2022 To be called in DeliveredMessagesReport ************/
	/**
	 * Returns the speed of the host
	 * @return The speed
	 */
	public double getNodeSpeed() {
		return this.speed;
	}
	/**************  SMK 27 Apr 2022 ************/
	
//Modified by Nupur - start
 /** public String[] getTrustVal()
 {
   return hmTrustVal;
 }
 public void setStringArray(String[] hmTrustVal) 
 {
    this.hmTrustVal = hmTrustVal;
 }
 **/
 public void setInitialTrustValueArr(List<DTNHost> hostNames)
   {
     
	 int size = hostNames.size();
     //hmTrustVal=Arrays.copyOf(hmTrustVal, size);
	 for(int i=0;i<size;i++)
       {
         String strCurrentHostName = hostNames.get(i).toString();
		 DTNHost dCurrentHost = hostNames.get(i);
		 //System.out.println(this.name);
		 //System.out.println(strCurrentHostName);
		 if (this.address==dCurrentHost.address)
                this.hmTrustVal[i]=-1;
            else 
                this.hmTrustVal[i]=1;

       }
   }
   public void updateNoOfDropped()
   {
	   noofDropped++;
	   
   }
   public void droppedMessageWithoutReason ()
   {
   }

   public void updateTrustValMap (DTNHost dHostNameValToSet, boolean bTrusted, boolean bDelivered) {
        Integer intTrustVal = new Integer(1);
		//System.out.println("Initial trust value" + intTrustVal);
        //if (this.hmTrustVal.containsKey(dHostNameValToSet.toString())) {
            intTrustVal = this.hmTrustVal[dHostNameValToSet.address];
            
            int iNewTrustVal = intTrustVal.intValue();
            if (bTrusted && bDelivered) {
                iNewTrustVal = intTrustVal.intValue() + 1;
				String s= dHostNameValToSet.toString();
			    boolean FLAG=false;
			
			
				for(int i=0; i<this.upto; i++)
				{
					if(s.equals(friendList[i]))
					{	
					   FLAG=true;
					   break;
					}
				}
				if(FLAG==false)
				{
					friendList[this.upto]=s;
				    this.upto+=1;
				}	
			
        
			}				 
		/*	else{
			if(bTrusted)
                     iNewTrustVal = intTrustVal.intValue() + 1;
		
            else     
		    		 iNewTrustVal = intTrustVal.intValue() - 1;
            }
		*/	
            this.hmTrustVal[dHostNameValToSet.address]= iNewTrustVal;
	// ************************* Displaying Friend lists and Trust values ******************** //	
            
    /*System.out.print("\nMap Updt: "+bTrusted+"; "+this.toString()+"="+ Arrays.toString(this.getTrustValMap()));
	try {
			Files.write(Paths.get("reports//Deliver.txt"),(this.toString()+"="+ Arrays.toString(this.getTrustValMap())+ "\n").getBytes(), StandardOpenOption.APPEND);
			} catch(IOException e) {
				System.out.println(e);
			}
	System.out.print("Friend list: "+this.toString()+"=[ ");
	String str= this.toString() + " ";
	for(int i=0; i<this.upto; i++){
		System.out.print(friendList[i] + "  ");
		str+= friendList[i] + " ";
	}
	try {
			Files.write(Paths.get("reports//Friendlist.txt"),(str+ "\n").getBytes(), StandardOpenOption.APPEND);
			} catch(IOException e) {
				System.out.println(e);
			}
	System.out.print("]");*/
      	
    }
   
public static void printAllHostsTrustValMap (List<DTNHost> hostNames) {
        for(int i=0; i<hostNames.size(); i++) {
            DTNHost dCurrentHost = hostNames.get(i);
            dCurrentHost.printSingleHostTrustValMap ();

        }
    }
	
	public static void printAllHostsNoOfMessageDropped(List<DTNHost> hostNames)
	{
		for(int i=0; i<hostNames.size(); i++) {
            DTNHost dCurrentHost = hostNames.get(i);
            //System.out.println("Host: "+dCurrentHost+ " Messages Dropped: " + dCurrentHost.noofDropped);
		}
	}

public int[] getTrustValMap()
{
  return this.hmTrustVal;
}

public String[] getFriendList()
{
  return this.friendList;	
}

    public void printSingleHostTrustValMap () {
        //System.out.println("Address: " + this.address);
		//System.out.println("Name: "+ this.name);
		//System.out.println("Host: "+this+ ": "+ Arrays.toString(this.hmTrustVal));
    }

	/**
	 * Returns a new network interface address and increments the address for
	 * subsequent calls.
	 * @return The next address.
	 */
	private synchronized static int getNextAddress() {
		return nextAddress++;
	}

	/**
	 * Reset the host and its interfaces
	 */
	public static void reset() {
		nextAddress = 0;
	}

	/**
	 * Returns true if this node is actively moving (false if not)
	 * @return true if this node is actively moving (false if not)
	 */
	public boolean isMovementActive() {
		return this.movement.isActive();
	}

	/**
	 * Returns true if this node's radio is active (false if not)
	 * @return true if this node's radio is active (false if not)
	 */
	public boolean isRadioActive() {
		// Radio is active if any of the network interfaces are active.
		for (final NetworkInterface i : this.net) {
			if (i.isActive()) return true;
		}
		return false;
	}

	/**
	 * Set a router for this host
	 * @param router The router to set
	 */
	private void setRouter(MessageRouter router) {
		router.init(this, msgListeners);
		this.router = router;
	}

	/**
	 * Returns the router of this host
	 * @return the router of this host
	 */
	public MessageRouter getRouter() {
		return this.router;
	}

	/**
	 * Returns the network-layer address of this host.
	 */
	public int getAddress() {
		return this.address;
	}

	/**
	 * Returns this hosts's ModuleCommunicationBus
	 * @return this hosts's ModuleCommunicationBus
	 */
	public ModuleCommunicationBus getComBus() {
		return this.comBus;
	}

    /**
	 * Informs the router of this host about state change in a connection
	 * object.
	 * @param con  The connection object whose state changed
	 */
	public void connectionUp(Connection con) {
		this.router.changedConnection(con);
	}

	public void connectionDown(Connection con) {
		this.router.changedConnection(con);
	}

	/**
	 * Returns a copy of the list of connections this host has with other hosts
	 * @return a copy of the list of connections this host has with other hosts
	 */
	public List<Connection> getConnections() {
		List<Connection> lc = new ArrayList<Connection>();

		for (NetworkInterface i : net) {
			lc.addAll(i.getConnections());
		}

		return lc;
	}

	/**
	 * Returns the current location of this host.
	 * @return The location
	 */
	public Coord getLocation() {
		return this.location;
	}

	/**
	 * Returns the Path this node is currently traveling or null if no
	 * path is in use at the moment.
	 * @return The path this node is traveling
	 */
	public Path getPath() {
		return this.path;
	}


	/**
	 * Sets the Node's location overriding any location set by movement model
	 * @param location The location to set
	 */
	public void setLocation(Coord location) {
		this.location = location.clone();
	}

	/**
	 * Sets the Node's name overriding the default name (groupId + netAddress)
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the messages in a collection.
	 * @return Messages in a collection
	 */
	public Collection<Message> getMessageCollection() {
		return this.router.getMessageCollection();
	}

	/**
	 * Returns the number of messages this node is carrying.
	 * @return How many messages the node is carrying currently.
	 */
	public int getNrofMessages() {
		return this.router.getNrofMessages();
	}

	/**
	 * Returns the buffer occupancy percentage. Occupancy is 0 for empty
	 * buffer but can be over 100 if a created message is bigger than buffer
	 * space that could be freed.
	 * @return Buffer occupancy percentage
	 */
	public double getBufferOccupancy() {
		long bSize = router.getBufferSize();
		long freeBuffer = router.getFreeBufferSize();
		return 100*((bSize-freeBuffer)/(bSize * 1.0));
	}

	/**
	 * Returns routing info of this host's router.
	 * @return The routing info.
	 */
	public RoutingInfo getRoutingInfo() {
		return this.router.getRoutingInfo();
	}

	/**
	 * Returns the interface objects of the node
	 */
	public List<NetworkInterface> getInterfaces() {
		return net;
	}

	/**
	 * Find the network interface based on the index
	 */
	public NetworkInterface getInterface(int interfaceNo) {
		NetworkInterface ni = null;
		try {
			ni = net.get(interfaceNo-1);
		} catch (IndexOutOfBoundsException ex) {
			throw new SimError("No such interface: "+interfaceNo +
					" at " + this);
		}
		return ni;
	}

	/**
	 * Find the network interface based on the interfacetype
	 */
	protected NetworkInterface getInterface(String interfacetype) {
		for (NetworkInterface ni : net) {
			if (ni.getInterfaceType().equals(interfacetype)) {
				return ni;
			}
		}
		return null;
	}

	/**
	 * Force a connection event
	 */
	public void forceConnection(DTNHost anotherHost, String interfaceId,
			boolean up) {
		NetworkInterface ni;
		NetworkInterface no;

		if (interfaceId != null) {
			ni = getInterface(interfaceId);
			no = anotherHost.getInterface(interfaceId);

			assert (ni != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
			assert (no != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
		} else {
			ni = getInterface(1);
			no = anotherHost.getInterface(1);

			assert (ni.getInterfaceType().equals(no.getInterfaceType())) :
				"Interface types do not match.  Please specify interface type explicitly";
		}

		if (up) {
			ni.createConnection(no);
		} else {
			ni.destroyConnection(no);
		}
	}

	/**
	 * for tests only --- do not use!!!
	 */
	/*public void connect(DTNHost h) {
		if (DEBUG) Debug.p("WARNING: using deprecated DTNHost.connect" +
			"(DTNHost) Use DTNHost.forceConnection(DTNHost,null,true) instead");
		forceConnection(h,null,true);
	}*/

	/**
	 * Updates node's network layer and router.
	 * @param simulateConnections Should network layer be updated too
	 */
	public void update(boolean simulateConnections) {
		if (!isRadioActive()) {
			// Make sure inactive nodes don't have connections
			tearDownAllConnections();
			return;
		}

		if (simulateConnections) {
			for (NetworkInterface i : net) {
				i.update();
			}
		}
		this.router.update();
	}

	/**
	 * Tears down all connections for this host.
	 */
	private void tearDownAllConnections() {
		for (NetworkInterface i : net) {
			// Get all connections for the interface
			List<Connection> conns = i.getConnections();
			if (conns.size() == 0) continue;

			// Destroy all connections
			List<NetworkInterface> removeList =
				new ArrayList<NetworkInterface>(conns.size());
			for (Connection con : conns) {
				removeList.add(con.getOtherInterface(i));
			}
			for (NetworkInterface inf : removeList) {
				i.destroyConnection(inf);
			}
		}
	}

	/**
	 * Moves the node towards the next waypoint or waits if it is
	 * not time to move yet
	 * @param timeIncrement How long time the node moves
	 */
	public void move(double timeIncrement) {
		double possibleMovement;
		double distance;
		double dx, dy;

		if (!isMovementActive() || SimClock.getTime() < this.nextTimeToMove) {
			return;
		}
		if (this.destination == null) {
			if (!setNextWaypoint()) {
				return;
			}
		}

		possibleMovement = timeIncrement * speed;
		distance = this.location.distance(this.destination);

		while (possibleMovement >= distance) {
			// node can move past its next destination
			this.location.setLocation(this.destination); // snap to destination
			possibleMovement -= distance;
			if (!setNextWaypoint()) { // get a new waypoint
				return; // no more waypoints left
			}
			distance = this.location.distance(this.destination);
		}

		// move towards the point for possibleMovement amount
		dx = (possibleMovement/distance) * (this.destination.getX() -
				this.location.getX());
		dy = (possibleMovement/distance) * (this.destination.getY() -
				this.location.getY());
		this.location.translate(dx, dy);
	}

	/**
	 * Sets the next destination and speed to correspond the next waypoint
	 * on the path.
	 * @return True if there was a next waypoint to set, false if node still
	 * should wait
	 */
	private boolean setNextWaypoint() {
		if (path == null) {
			path = movement.getPath();
		}

		if (path == null || !path.hasNext()) {
			this.nextTimeToMove = movement.nextPathAvailable();
			this.path = null;
			return false;
		}

		this.destination = path.getNextWaypoint();
		this.speed = path.getSpeed();

		if (this.movListeners != null) {
			for (MovementListener l : this.movListeners) {
				l.newDestination(this, this.destination, this.speed);
			}
		}

		return true;
	}

	/**
	 * Sends a message from this host to another host
	 * @param id Identifier of the message
	 * @param to Host the message should be sent to
	 */
	public void sendMessage(String id, DTNHost to) {
		this.router.sendMessage(id, to);
	}

	/**
	 * Start receiving a message from another host
	 * @param m The message
	 * @param from Who the message is from
	 * @return The value returned by
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public int receiveMessage(Message m, DTNHost from) {
		
		int retVal = this.router.receiveMessage(m, from);
		int msg = 0;
		//int x = 60;
		//String [][] rcv_msg = new String[x][4];
		//int i = 0; 
		//List<String[]> rowList = new ArrayList<String[]>();
		//if(FLAG){
		if (retVal == MessageRouter.RCV_OK) {
			m.addNodeOnPath(this);	// add this node on the messages path
			
			//System.out.println("\n" + this.toString() + " receives message " + m.getId() + " from " + from.toString());
			
			// ************ SMK 8 Aug 2019 **************
			
			try {
			/*Files.write(Paths.get("reports//Receive.txt"),("\n" + this.toString() + 
					" receives message " + m.getId() + " from " + from.toString() + 
					"  at time " + SimClock.getTime() + "** i ** " + i).getBytes(), StandardOpenOption.APPEND);*/
			//Files.write(Paths.get("reports//Testing.txt"),("\n Hello").getBytes(), StandardOpenOption.APPEND);
			Files.write(Paths.get("reports//Testing.txt"),("\n" + this.toString() + 
					" receives message " + m.getId() + " from " + from.toString() + 
					"  at time " + SimClock.getTime() + "** i ** " + i).getBytes(), StandardOpenOption.APPEND);
			   
					//rowList.add(new String[] { this.toString(), m.getId(), from.toString(),Double.toString(SimClock.getTime())});
					//System.out.println("i ======= "+ i);
					rcv_msg[i][0] = from.toString();
					rcv_msg[i][1] = m.getId();
					rcv_msg[i][2] = this.toString();
					rcv_msg[i][3] = Double.toString(SimClock.getTime()); 
					i++;
				
			} catch(IOException e) {
				System.out.println(e);
			}
			// ************ SMK 8 Aug 2019  END **************
          }
		
		
		//}
		/*else{
			System.out.println(this.toString() + " cannot receive " + m.getId() + " from " + from.toString());
		}*/
		/*for (int t = 0; t<x; t++){
			for (int j = 0; j<4; j++){
				System.out.print(rcv_msg[t][j] + " --> ");
			}
			System.out.println();
		}*/
		
		/* Messages dropped by bad nodes  */
		for (int t = 0; t<x; t++){
			//System.out.print(" Inside bad node displayyyyy ");
			//String s2 = new String("p24");
			//List<String> bad = Arrays.asList("p24","p16","p4","p30","p9","p32","p51","p47","p88","p39","p108","p0","p122","p139","p77","p69","p94","p143","p152","p190");
			
			List<String> bad = Arrays.asList("p24","p16","p4","p30","p9","p32","p51","p47","p88","p39","p108","p0","p122","p139","p77","p69","p94","p143","p152","p190");
			//for (int j = 0; j<4; j++){
				//System.out.print(rcv_msg[t][j] + " --> ");
				//if(rcv_msg[t][0]!=null & ((rcv_msg[t][0].equals("p24") == true) | (rcv_msg[t][0].equals("p16") == true))){   // if bad node then delete the msg
				if(rcv_msg[t][0]!=null && bad.contains(rcv_msg[t][0])){          // if bad node then delete the msg
					//System.out.println("Bad: "+rcv_msg[t][0]);
					if(m.getId()!= null){
						msg++;
						this.deleteMessage(m.getId(), false);
					
					}
				}
				
			//}
			//System.out.println();
		} 
		
		//## System.out.println("msgs dropped = " + msg);
		//--if (SimClock.getTime() >= 43197){  // for 50 nodes        
		if (SimClock.getTime() >= 43190){  
		//if (SimClock.getTime() >= 43156){  //for 25 nodes + 2 bad nodes
			
			HashMap<String, Double> forwardTrustVal = displayNode();
			
			//************** SMK 21 Apr 2022 ****************
			/*System.out.println("The list of potential Gray hole nodes are: %%%%%%%%%%%%%%% ");
			 for (Map.Entry<String,Double> entry : forwardTrustVal.entrySet()) { 
		         if(entry.getValue() == 0.0)
		         {
		        	 System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
		         }	
			 }*/
			//************** SMK 21 Apr 2022 ****************
		}
		return retVal;		 
	}

	
		public HashMap<String, Double> displayNode(){
			
			HashMap<String, Double> trustVal = new HashMap<String, Double>();
			//List<String> bad = Arrays.asList("p24","p16","p4","p30","p9","p32","p51","p47","p88","p39","p108","p0","p122","p139","p77","p69","p94","p143","p152","p190");
			 
			 List<String> bad = Arrays.asList("p24","p16","p4","p30","p9","p32","p51","p47","p88","p39","p108","p0","p122","p139","p77","p69","p94","p143","p152","p190");
			//************** SMK 7 June 2022 ****************
			HashMap<String, Double> energyVal = new HashMap<String, Double>();
			double initEnergy = 500;  //initial energy level in joules
			
			//************** SMK 7 June 2022 ****************
			double totalTrust = 0;
			
			int flag = 0;
			//System.out.println("In displayNode function");
			// Display the matrix (from, msg, to, @time)
			/*for (int t = 0; t<x; t++){
				for (int j = 0; j<4; j++){
					System.out.print(rcv_msg[t][j] + " --> ");
				}
				System.out.println();
			}*/
			 int row = rcv_msg.length;
			 //int col = rcv_msg[0].length;
			 for (int p = 0; p < row; p++){
				 for (int q = p+1; q < row; q++){
					 if(rcv_msg[p][2] != null && rcv_msg[q][0] != null){
						 
					//checking if rcv_msg[p][2] has further forwarded msg, hence non-malicious
					 if((rcv_msg[p][2] == rcv_msg[q][0]) && (rcv_msg[p][1] == rcv_msg[q][1])){ 
						
						 //System.out.println("##" + rcv_msg[p][0] + "sends msg " + rcv_msg[p][1] + " to " + rcv_msg[p][2] + " and " + rcv_msg[q][0] +
						 // " further forwards msg " + rcv_msg[q][1] + " at time: " + rcv_msg[p][3]);
						
						 if(trustVal.containsKey(rcv_msg[p][2]) == true){  //if already this node is in good list
							 totalTrust = trustVal.get(rcv_msg[p][2]);
						 }
						 else {
							 totalTrust = 0;                  //else this node is forwarding for the first time
						 }
						//************** SMK 7 June 2022 ****************
						 if(energyVal.containsKey(rcv_msg[p][2]) == true){  //if already this node is in good list
							 initEnergy = energyVal.get(rcv_msg[p][2]);
						 }
						 else {
							 initEnergy = 500;                  //else this node is forwarding for the first time
						 }
						 
						  //List<String> bad = Arrays.asList("p24","p16","p4","p30","p9");
						 if(!bad.contains(rcv_msg[p][2]))	{ 
						 	trustVal.put(rcv_msg[p][2],totalTrust+0.5); // node and its trust value updated in list
						 	//if(energyVal.containsKey(rcv_msg[p][2]) == true && energyVal.get(rcv_msg[p][2])>= 20)
						 	energyVal.put(rcv_msg[p][2],initEnergy - 20);  // node's energy level reduces by 20 joules on every msg transmission, if becomes negative, its set at 0
						 	
						 	//************** SMK 7 June 2022 ****************
						 	
						 // ************ SMK 21 Apr 2022 **************
						/*try {
						 	Files.write(Paths.get("reports//Fwd_Test.txt"),("\n" + rcv_msg[p][2] + " --- > yes ").getBytes(), StandardOpenOption.APPEND);
					    } catch(IOException e) {
							System.out.println(e);
						}*/
						// ************ SMK 21 Apr 2022 **************
						 flag = 1;
						 //System.out.println(rcv_msg[p][2] + "  is Not Malicious!! ******  trust value is : " + 
								// trustVal.get(rcv_msg[p][2]) + " flag is " + flag);
						 //System.out.println(rcv_msg[p][2] + "  is Not Malicious!! and trust value is : " + totalTrust);
						 
					 } 
				   }
				 }
			 }
				
				//************** SMK 7 June 2022 ****************
				 if (trustVal.containsKey(rcv_msg[p][2]) == false) {
					 if(rcv_msg[p][2] != null) {
						 trustVal.put(rcv_msg[p][2],0.0);       // trust value set to 0 if the node doesn't forward msg
						 //if(bad.contains(rcv_msg[p][2]) && energyVal.containsKey(rcv_msg[p][2]) == false)
						 energyVal.put(rcv_msg[p][2],initEnergy - 10); //node's energy level reduces by 10 joules if it only receives but does not forward msgs
					
						 // ************ SMK 21 Apr 2022 **************
					 /*try {
						  Files.write(Paths.get("reports//Fwd_Test.txt"),("\n" + rcv_msg[p][2] + " --- > no ").getBytes(), StandardOpenOption.APPEND);
					    } catch(IOException e) {
							System.out.println(e);
						}*/
					 }
					// ************ SMK 21 Apr 2022 **************
				 }
		 }
			 //--System.out.println("Trustvalue $$$$$$$$$ (Forward Trust) " + trustVal.toString());
			 
			 // Displaying the nodes with trust value 0.0
			 /*System.out.println("The list of potential Gray hole nodes are:");
			 for (Map.Entry<String,Double> entry : trustVal.entrySet()) { 
		         if(entry.getValue() == 0.0)
		         {
		        	 System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
		         }	
			 }*/
			//************** SMK 7 June 2022 ****************
			 // Displaying the nodes and energy values
			 /*for (Map.Entry<String,Double> entry : energyVal.entrySet()) { 
		      System.out.println("Node = " + entry.getKey() + ", Energy Value = " + entry.getValue());     	
			 }*/
			 // Displaying the nodes and energy values
			 try {
				 for (Map.Entry<String,Double> en : energyVal.entrySet()) { 
				 	Files.write(Paths.get("reports//Energy_200nodes.txt"),("\n" + en.getKey() + 
							" " + en.getValue()).getBytes(), StandardOpenOption.APPEND);
				 	}
				 	Files.write(Paths.get("reports//Energy_200nodes.txt"),("\n" + "-------------------------- ").getBytes(), 
				 			StandardOpenOption.APPEND);
			    } catch(IOException e) {
					System.out.println(e);
			}
			//************** SMK 7 June 2022 ****************
			return trustVal;
		}
	// ********************** SMK 9 Aug 2019  END ***********************
	
	
	/**
	 * Requests for deliverable message from this host to be sent trough a
	 * connection.
	 * @param con The connection to send the messages trough
	 * @return True if this host started a transfer, false if not
	 */
	public boolean requestDeliverableMessages(Connection con) {
		return this.router.requestDeliverableMessages(con);
	}

	/**
	 * Informs the host that a message was successfully transferred.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 */
	public void messageTransferred(String id, DTNHost from) {
		this.router.messageTransferred(id, from);
	}

	/**
	 * Informs the host that a message transfer was aborted.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 * @param bytesRemaining Nrof bytes that were left before the transfer
	 * would have been ready; or -1 if the number of bytes is not known
	 */
	public void messageAborted(String id, DTNHost from, int bytesRemaining) {
		this.router.messageAborted(id, from, bytesRemaining);
		//System.out.println("\nThe message id: " + id + " from host " + from + " is aborted by " + this);
	}

	/**
	 * Creates a new message to this host's router
	 * @param m The message to create
	 */
	public void createNewMessage(Message m) {
		this.router.createNewMessage(m);
	}

	/**
	 * Deletes a message from this host
	 * @param id Identifier of the message
	 * @param drop True if the message is deleted because of "dropping"
	 * (e.g. buffer is full) or false if it was deleted for some other reason
	 * (e.g. the message got delivered to final destination). This effects the
	 * way the removing is reported to the message listeners.
	 */
	
	//******************* SMK 20 Sept 2019 *************************//
	public void deleteMessage(String id, boolean drop) {
		this.router.deleteMessage(id, drop);
	}
	
	
	//******************* SMK 20 Sept 2019 END *************************//
	
	
	
	/**
	 * Returns a string presentation of the host.
	 * @return Host's name
	 */
	public String toString() {
		return name;
	}
	public static DTNHost toDTNHost(String name)
	{
		return new DTNHost(name);
	}

	/**
	 * Checks if a host is the same as this host by comparing the object
	 * reference
	 * @param otherHost The other host
	 * @return True if the hosts objects are the same object
	 */
	public boolean equals(DTNHost otherHost) {
		return this == otherHost;
	}

	/**
	 * Compares two DTNHosts by their addresses.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(DTNHost h) {
		return this.getAddress() - h.getAddress();
	}

}
