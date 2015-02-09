
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
    private NodeClocks[] node_clocks = new NodeClocks[3];
    
    // Vector Clock Size
    private int num;
    
    // Constructor
    public Vector(String[] nodes, String meName) {
        d = 1;
        my_ts = 1;
        my_name = meName;
        num = nodes.length;
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
    	String new_ts = node_clocks[0].node_name + "=" + node_clocks[0].node_ts + "," +
    					node_clocks[1].node_name + "=" + node_clocks[1].node_ts + "," +
    					node_clocks[2].node_name + "=" + node_clocks[2].node_ts + "," +
    					my_name + "=" + my_ts;
    	TimeStamp send_ts = new TimeStamp(new_ts);
    	send_ts.ts = new_ts;
    	return send_ts;
    }
    
    @ Override
    // Get timestamp for non-message
    public TimeStamp get_clockTimeStamp() {
    	String new_ts = node_clocks[0].node_name + "=" + node_clocks[0].node_ts + 
				node_clocks[1].node_name + "=" + node_clocks[1].node_ts + 
				node_clocks[2].node_name + "=" + node_clocks[2].node_ts + 
				my_name + "=" + my_ts;
		TimeStamp send_ts = new TimeStamp(new_ts);
		send_ts.ts = new_ts;
		return send_ts;
    }
}
