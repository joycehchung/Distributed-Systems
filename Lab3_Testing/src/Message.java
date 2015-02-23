import java.io.*;


public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private int sequenceNumber;
    private String src;
    private String dest;
    private String kind;
    private Object data;
    private boolean duplicate;
 
    // This is just to keep track of socket to send/receive messages from
    private int nodeIndex;
    
    // This is just to keep track of multicast messages
    private boolean multicast;
    private boolean remulticast;

    public Message(String dest, String kind, Object data) {
        this.dest = dest;
        this.kind = kind;
        this.data = data;
        this.duplicate = false;
        this.multicast = false;
        this.remulticast = false;
    }

    public int get_seqNum() {
        return sequenceNumber;
    }

    public void set_seqNum(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String get_source() {
        return src;
    }

    public void set_source(String src) {
        this.src = src;
    }

    public String get_destination() {
        return dest;
    }

    public void set_destination(String dest) {
        this.dest = dest;
    }

    public String get_kind() {
        return kind;
    }

    public void set_kind(String kind) {
        this.kind = kind;
    }

    public Object get_data() {
        return data;
    }

    public void set_data(Object data) {
        this.data = data;
    }

    public int get_nodeIndex() {
        return nodeIndex;
    }

    public void set_nodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public Boolean get_duplicate() {
        return duplicate;
    }

    public void set_duplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }
    
    public Boolean get_multicast() {
        return multicast;
    }

    public void set_multicast(boolean multicast) {
        this.multicast = multicast;
    }
    
    public Boolean get_remulticast() {
        return remulticast;
    }

    public void set_remulticast(boolean remulticast) {
        this.remulticast = remulticast;
    }


    @Override
    public String toString() {
        return "From:" + this.get_source() + 
        		" To:" + this.get_destination() +
        		" Seq:" + this.get_seqNum() + 
        		" Kind:" + this.get_kind() +
        		" Dup:" + this.get_duplicate() + 
        		" Data:" + this.get_data();
    }
}

class TimeStampedMessage extends Message implements Comparable<TimeStampedMessage> {

	private static final long serialVersionUID = 1L;
	
	private TimeStamp msgTimeStamp;
	private int globalClock;
	
	public TimeStampedMessage(String dest, String kind, Object data) {
		super(dest, kind, data);
	}
	
	public void set_timeStamp(TimeStamp ts) {
		this.msgTimeStamp = ts;
	}
	
	public TimeStamp get_timeStamp() {
		return msgTimeStamp;
	}
	
	public void set_globalClock(int time) {
		this.globalClock = time;
	}
	
	public int get_globalClock() {
		return globalClock;
	}
	
	public boolean checkEquals(TimeStamp ts) {
		return this.msgTimeStamp.equals(ts);
	}

	@Override
	// negative if comp_msg is > this
	public int compareTo(TimeStampedMessage comp_msg) {
//		String[] partsA = this.get_data().toString().split(",");
//		String[] partsB = comp_msg.get_data().toString().split(",");
//		int val = 0;
//		if (partsA[0].compareToIgnoreCase(partsB[0]) == 0) {
//			val = partsA[1].compareToIgnoreCase(partsB[1]);
//		} else {
//			val = partsA[0].compareToIgnoreCase(partsB[0]);
//		}
//		return val;
		return (int)(this.get_timeStamp().ts.compareToIgnoreCase(comp_msg.get_timeStamp().ts));
	}
	
    @Override
    public String toString() {
        return	"From:" + this.get_source() + 
        		" To:" + this.get_destination() +
        		" TimeStamp:" + this.get_timeStamp().ts + 
        		" Seq:" + this.get_seqNum() + 
        		" Kind:" + this.get_kind() +
        		" Dup:" + this.get_duplicate() + 
        		" Data:" + this.get_data();
	}
}
