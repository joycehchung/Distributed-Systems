
// ClockService implemented as a singleton
public class ClockService {
	   private static ClockService instance = null;    
	   
	   protected ClockService() {}
	   
	   
	   public enum ClockTypes{
		   LOGICAL, VECTOR, NONE
	   }
	   
	   
	   public static ClockService CreateClockService(ClockTypes type, String[] nodes, String meName) {
	      if(instance == null) {
	    	  switch(type){
	    	  	case LOGICAL:
	    	  		instance = new Logical();
	    	  		break;
	    	  	case VECTOR: 
	    	  		instance = new Vector(nodes, meName);
	    	  		break;
				default:
					break;  		  
	    	  }
	      }
	      return instance;
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