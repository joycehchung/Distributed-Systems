import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

// ClockService implemented as a singleton
public class ClockService {
	   private static ClockService instance = null;    
	   
	   protected ClockService() {}
	   
	   
	   public enum ClockTypes{
		   LOGICAL, VECTOR, NONE
	   }
	   
	   
	   public static ClockService CreateClockService(ClockTypes type) {
	      if(type == null) {
	    	  switch(type){
	    	  case LOGICAL:
	    		  instance = new Logical();
	    	  case VECTOR: 
	    		  instance = new Vector();  		  
	    		  return instance;
				default:
					return null;
	    	  }
	      }
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
