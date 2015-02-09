public class Logical extends ClockService {
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
