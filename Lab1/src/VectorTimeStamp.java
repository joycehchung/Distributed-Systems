

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VectorTimeStamp extends TimeStamp {
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> timevector;

    public VectorTimeStamp(ArrayList<String> hosts) {
        this.timevector = new HashMap<String, Integer>();
        for (String name : hosts) {
            this.timevector.put(name, 0);
        }
    }
    
    public VectorTimeStamp(VectorTimeStamp vts_) {
    	timevector = new HashMap<String, Integer>();
    	for(String s : vts_.getVectorTime().keySet()){
    		timevector.put(s, vts_.getVectorTime().get(s));
    	}
    }

    public int compareTo(TimeStamp ts) {
        if (ts instanceof VectorTimeStamp) {
            VectorTimeStamp vectorts = (VectorTimeStamp) ts;
            boolean less = false;
            boolean greater = false;
            for (String name : this.timevector.keySet()) {
                if (this.getTime(name) > vectorts.getTime(name)) {
                    greater = true;
                }
                if (this.getTime(name) < vectorts.getTime(name)) {
                    less = true;
                }
            }
            if (greater && !less) {
                return 1;
            } 
            else if (greater && less) { //Concurrent
                return 0;
            } 
            else if (!greater && less) {
                return -1;
            } 
            else {
                return 0;
            }
        } else {
            System.out.println("TimeStamp is incorrect");
            return -2;
        }
    }

    public Map<String, Integer> getVectorTime() {
        return this.timevector;
    }

    public int getTime(String host) {
        if (!this.timevector.containsKey(host)) {
            return -1;
        }
        return this.timevector.get(host);
    }

    public void setTime(VectorTimeStamp vts) {
        for (String name : this.timevector.keySet()) {
            this.timevector.put(name, Math.max(vts.getTime(name), this.getTime(name)));
        }
    }

    public void setTime(String host, int time) {
        if (!timevector.containsKey(host)) {
            return;
        }
        this.timevector.put(host, time);
    }

	@Override
	public String toString() {
		return "VectorTimeStamp"+  timevector;
	}

	@Override
	public int getLogicalTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
    
    
}

