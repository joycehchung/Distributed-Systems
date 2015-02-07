// ClockService implemented as a singleton
public class ClockService {
	   private static ClockService cs = null;
	   
	   protected ClockService() {}
	   
	   public static ClockService CreateClockService(String logical_or_vector) {
	      if(cs == null) {
	    	  if (logical_or_vector.equals("logical")) {
	    		  cs = new Logical();
	    	  } else if (logical_or_vector.equals("vector")) {
	    		  cs = new Vector();
	    	  }
	      }
	      return cs;
	   }
}

class Logical extends ClockService {
    private int d;
    private TimeStamp ts;
    
    public Logical() {
        this.d = 1;
        this.ts.timeStamp = 0;
    }

    // Call this method to increment process timestamp between every successive event
    public void updateTimeStamp() {
        this.ts.timeStamp += this.d;
    }
    
    // Get timestamp for message (received)
    public TimeStamp get_receiveTimeStamp(TimeStamp ts) {
    	this.ts.timeStamp = Math.max(this.ts.timeStamp, ts.timeStamp) + this.d;
    	return this.ts;
    }
    
    // Get timestamp for message (sent)
    public TimeStamp get_sendTimeStamp() {
    	return ts;
    }
    
    // Get timestamp for non-message
    public TimeStamp get_timeStamp() {
    	return ts;
    }
}

class Vector extends ClockService {
	
}