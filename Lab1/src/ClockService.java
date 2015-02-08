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
    private TimeStamp ts;
    
    public Logical() {
        this.d = 1;
        this.ts = new TimeStamp();
        this.ts.timeStamp = String.valueOf(1);
    }

    @ Override
    // Call this method to increment process timestamp between every successive event
    public void updateTimeStamp() {
        this.ts.timeStamp = String.valueOf(Integer.parseInt(this.ts.timeStamp) + this.d);
    }
    
    @ Override
    // Set / Update timestamp when message received
    public void set_receiveTimeStamp(TimeStamp ts) {
    	this.ts.timeStamp = String.valueOf(Math.max(Integer.parseInt(this.ts.timeStamp), Integer.parseInt(ts.timeStamp)) + this.d);
    }
    
    @ Override
    // Get timestamp for message (sent)
    public TimeStamp get_sendTimeStamp() {
    	return ts;
    }
    
    @ Override
    // Get timestamp for non-message
    public TimeStamp get_clockTimeStamp() {
    	return ts;
    }
}

class Vector extends ClockService {
	
}