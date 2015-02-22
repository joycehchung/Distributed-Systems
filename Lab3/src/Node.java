import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Node class
public class Node {
    public String name;
    public String ip;
    public int port;
}

class MyNode extends Node {
    public int numOtherNodes;
    public int nodeNumber;
    public ArrayList<ObjectInputStream> in = new ArrayList<ObjectInputStream>();
    public ArrayList<ObjectOutputStream> out = new ArrayList<ObjectOutputStream>();
    public ObjectInputStream[] inS;
    public ObjectInputStream[] inC;
    public ObjectOutputStream[] outS;
    public ObjectOutputStream[] outC;
    public BufferedReader[] inbox = new BufferedReader[3];
    public PrintWriter[] outbox = new PrintWriter[3];
    public BufferedReader[] inboxC = new BufferedReader[3];
    public PrintWriter[] outboxC = new PrintWriter[3];
    public BlockingQueue<TimeStampedMessage> receiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedReceiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedSendQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    
//    public Node(int nServers, int nClients) {
//    	inS = new ObjectInputStream[nServers];
//    	inC = new ObjectInputStream[nClients];
//    	outS = new ObjectOutputStream[nServers];
//    	outC = new ObjectOutputStream[nClients];
//    }
}

