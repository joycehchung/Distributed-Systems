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
    public BlockingQueue<TimeStampedMessage> receiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedReceiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedSendQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> requestQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public String myVotingSet = "";
    public int myVotingSetNumber;
}

