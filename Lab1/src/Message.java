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

    public Message(String dest, String kind, Object data) {
        this.dest = dest;
        this.kind = kind;
        this.data = data;
        this.duplicate = false;
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

class TimeStampedMessage extends Message {

	TimeStamp timeStamp;
	
	public TimeStampedMessage(String dest, String kind, Object data) {
		super(dest, kind, data);
	}
	
	public void set_timeStamp(TimeStamp ts) {
		this.timeStamp = ts;
	}
	
	public TimeStamp get_timeStamp() {
		return timeStamp;
	}
	
	public boolean checkEquals(TimeStamp ts) {
		return this.timeStamp.equals(ts);
	}
	
    @Override
    public String toString() {
        return	"From:" + this.get_source() + 
        		" To:" + this.get_destination() +
        		" TimeStamp:" + this.get_timeStamp().timeStamp + 
        		" Seq:" + this.get_seqNum() + 
        		" Kind:" + this.get_kind() +
        		" Dup:" + this.get_duplicate() + 
        		" Data:" + this.get_data();
    }
}
