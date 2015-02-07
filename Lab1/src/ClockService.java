
public class ClockService {

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