import java.util.Arrays;


public class Vector extends ClockService {
	
	// My information
    private int d;
    private int my_ts;
    private String my_name;
    
    // All other node information
    public class NodeClocks {
    	public String node_name;
    	public int node_ts;
    }
    private NodeClocks[] node_clocks; // = new NodeClocks[3];
    
    // Relationship Integers
    private final static int EQUAL = 0;
    private final static int BEFORE = 1;
    private final static int AFTER = 2;
    private final static int CONCURRENT = 3;
    private final static int ERROR = 4;
    
    // Vector Clock Size
    private int num;
    
    // Constructor
    public Vector(String[] nodes, String meName) {
    	
        d = 1;
        my_ts = 0;
        my_name = meName;
        
        // Nodes
        Arrays.sort(nodes);
        num = nodes.length;
        node_clocks = new NodeClocks[num];
        for (int i=0; i<num; i++) {
        	node_clocks[i] = new NodeClocks();
        	node_clocks[i].node_name = nodes[i];
        	node_clocks[i].node_ts = 0;
        }
    }
    
    @ Override
    // Call this method to increment process timestamp between every successive event
    public void updateTimeStamp() {
    	my_ts = my_ts + d;
    }
    
    @ Override 
    // Set / Update timestamp when Multicast message received
    public void set_receiveMulticastTimeStamp(TimeStamp msg_ts, String source) {
    	String[] parts = msg_ts.ts.split(",");
    	for (int i=0; i<parts.length; i++) {
    		String aname = parts[i].substring(0, parts[i].indexOf("="));
    		if (aname.equalsIgnoreCase(source)) {
	    		for (int j=0; j<num; j++) {
	    			if (node_clocks[j].node_name.equalsIgnoreCase(aname)) {
	    				node_clocks[j].node_ts = node_clocks[j].node_ts + 1;
	    				break;
	    			}
	    		}
    		}
    	}
    }
    
    @ Override
    // Set / Update timestamp when message received
    public void set_receiveTimeStamp(TimeStamp msg_ts) {
    	String[] parts = msg_ts.ts.split(",");
    	for (int i=0; i<parts.length; i++) {
    		String aname = parts[i].substring(0, parts[i].indexOf("="));
    		int atimestamp = Integer.parseInt(parts[i].substring(parts[i].indexOf("=")+1,parts[i].length()));
    		for (int j=0; j<num; j++) {
    			if (node_clocks[j].node_name.equalsIgnoreCase(aname)) {
    				node_clocks[j].node_ts = Math.max(node_clocks[j].node_ts, atimestamp);
    				break;
    			}
    		}
    	}
    }
    
    @ Override
    // Get timestamp for message (sent)
    public TimeStamp get_sendTimeStamp() {
    	boolean addedName = false;
       	String new_ts = "";
    	for (int i=0; i<node_clocks.length; i++) {
    		if (node_clocks[i].node_name.compareTo(my_name) > 0 && !addedName) {
    			new_ts = new_ts + my_name + "=" + my_ts + ",";
    			addedName = true;
    		}
    		if (i == node_clocks.length-1) {
    			new_ts = new_ts + node_clocks[i].node_name + "=" + node_clocks[i].node_ts;
    		} else {
    			new_ts = new_ts + node_clocks[i].node_name + "=" + node_clocks[i].node_ts + ",";
    		}
    	}
    	if (!addedName) {
    		new_ts = new_ts + "," + my_name + "=" + my_ts;
    	}
    	TimeStamp send_ts = new TimeStamp(new_ts);
    	send_ts.ts = new_ts;
    	return send_ts;
    }
    
    @ Override
    // Get timestamp for non-message
    public TimeStamp get_clockTimeStamp() {
    	boolean addedName = false;
       	String new_ts = "";
    	for (int i=0; i<node_clocks.length; i++) {
    		if (node_clocks[i].node_name.compareTo(my_name) > 0 && !addedName) {
    			new_ts = new_ts + my_name + "=" + my_ts + ",";
    			addedName = true;
    		}
    		if (i == node_clocks.length-1) {
    			new_ts = new_ts + node_clocks[i].node_name + "=" + node_clocks[i].node_ts;
    		} else {
    			new_ts = new_ts + node_clocks[i].node_name + "=" + node_clocks[i].node_ts + ",";
    		}
    	}
    	if (!addedName) {
    		new_ts = new_ts + "," + my_name + "=" + my_ts;
    	}
    	TimeStamp send_ts = new TimeStamp(new_ts);
    	send_ts.ts = new_ts;
    	return send_ts;
    }
    
    // Use to normalize timestamps being compared
    public String NormalizeTimeStamp(TimeStamp a) {
    	String[] a_msg = a.ts.split(",");
    	Arrays.sort(a_msg);
    	String[] nameTimeA;
    	String newa=""; 
    	for (int i=0; i<a_msg.length; i++) {
    		nameTimeA = a_msg[i].split("=");
    		if(i == a_msg.length-1) {
    			newa = newa + nameTimeA[1];
    		} else {
    			newa = newa + nameTimeA[1] + ",";
    		}
    	}
    	return newa;
    }
    
    @ Override
    // Use the check for extra wait condition for hold queue
    // Returns true if wait, false if no longer have to wait
    public boolean CheckWait(TimeStamp source_ts, String source) {
    	boolean wait = true;
    	
    	String[] parts = source_ts.ts.split(",");
    	for (int i=0; i<parts.length; i++) {
    		String aname = parts[i].substring(0, parts[i].indexOf("="));
    		int atimestamp = Integer.parseInt(parts[i].substring(parts[i].indexOf("=")+1,parts[i].length()));
    		if (aname.equalsIgnoreCase(source)) {
	    		for (int j=0; j<num; j++) {
	    			if (node_clocks[j].node_name.equalsIgnoreCase(aname)) {
	    				if (node_clocks[j].node_ts + 1 == atimestamp) {
	    					wait = false;
	    				}
	    				break;
	    			}
	    		}
    		}
    	}

		return wait;
    }
    @ Override
    // Use to check for causal ordering for messages in same group holdqueue
    public int get_causalOrder(TimeStamp a_ts, TimeStamp b_ts) {
    	String a = NormalizeTimeStamp(a_ts);
    	String b = NormalizeTimeStamp(b_ts);
    	
    	String[] a_msg = a.split(",");
    	String[] b_msg = b.split(",");
    	
    	if (a.equals(b)) {
    		return EQUAL;
    	} else {
    		boolean pos = false;
    		boolean neg = false;
	    	for (int i=0; i<a_msg.length; i++) {
				// Check if A <- B 
				if ((Integer.parseInt(a_msg[i])-Integer.parseInt(b_msg[i])) > 0) {
					pos = true;
				}
				// Check if A -> B
				if ((Integer.parseInt(a_msg[i])-Integer.parseInt(b_msg[i])) < 0) {
					neg = true;
				}
	    	}
			if (pos && neg) {
				return CONCURRENT;
			} else if (pos && !neg) {
				return AFTER;
			} else if (!pos && neg) {
				return BEFORE;
			}
    	}
    	return ERROR;
    }
    
}
