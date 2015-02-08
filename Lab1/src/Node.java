import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Node class
public class Node {
    public String name;
    public String ip;
    public int port;
    public int numNodes = 0;
    public int nodeNumber = 0;
    public BufferedReader[] inbox = new BufferedReader[3];
    public PrintWriter[] outbox = new PrintWriter[3];
    public BufferedReader[] inboxC = new BufferedReader[3];
    public PrintWriter[] outboxC = new PrintWriter[3];
    public BlockingQueue<TimeStampedMessage> receiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedReceiveQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    public BlockingQueue<TimeStampedMessage> delayedSendQueue = new LinkedBlockingQueue<TimeStampedMessage>();
}

