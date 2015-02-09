class LogicalClockService extends ClockService {
    private int d;
    private LogicalTimeStamp clock_ts;
    
    public LogicalClockService() {
        d = 1;
        this.clock_ts = new LogicalTimeStamp(0);
    }

    @ Override
    // Call this method to increment process timestamp between every successive event
    public void updateTimeStamp() {
    	this.clock_ts.setTime(clock_ts.getLogicalTime() + d);
    }
    
    @ Override
    // Set / Update timestamp when message received
    public void set_receiveTimeStamp(TimeStamp msg_ts) {
    if (msg_ts instanceof LogicalTimeStamp) {
            LogicalTimeStamp new_lts = (LogicalTimeStamp) msg_ts;
            this.clock_ts.setTime(new_lts.getLogicalTime());
            this.updateTimeStamp();
    } else {
            System.out.println("Invalid TimeStamp!");
    }
    
    
    }
    @ Override
    // Get timestamp for message (sent)
    public TimeStamp get_sendTimeStamp() {
    	return clock_ts;
    }
    
    @ Override
    // Get timestamp for non-message
    public TimeStamp get_clockTimeStamp() {
    	return clock_ts;
    }
    
    
}
