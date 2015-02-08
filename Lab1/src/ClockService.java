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
	   
		//*** Methods that will be overridden by Logical or Vector subclass methods: ***//
			// Call this method to increment process timestamp between every successive event
			protected void updateTimeStamp() { }
		    
			// Get timestamp for message (received)
			protected void set_receiveTimeStamp(TimeStamp ts) { }
			
			// Get timestamp for message (sent)
			protected TimeStamp get_sendTimeStamp() { return null; }
			
			// Get timestamp for non-message
			protected TimeStamp get_clockTimeStamp() { return null; }
}

class Logical extends ClockService {
    private int d;
    private int clock_ts;
    
    public Logical() {
        d = 1;
        clock_ts = 1;
    }

    @ Override
    // Call this method to increment process timestamp between every successive event
    public void updateTimeStamp() {
    	clock_ts = clock_ts + d;
    }
    
    @ Override
    // Set / Update timestamp when message received
    public void set_receiveTimeStamp(TimeStamp msg_ts) {
    	clock_ts = Math.max(clock_ts, Integer.parseInt(msg_ts.ts)) + d;
    }
    
    @ Override
    // Get timestamp for message (sent)
    public TimeStamp get_sendTimeStamp() {
    	TimeStamp send_ts = new TimeStamp();
    	send_ts.ts = String.valueOf(clock_ts);
    	return send_ts;
    }
    
    @ Override
    // Get timestamp for non-message
    public TimeStamp get_clockTimeStamp() {
    	TimeStamp send_ts = new TimeStamp();
    	send_ts.ts = String.valueOf(clock_ts);
    	return send_ts;
    }
}

class Vector extends ClockService {
	
}