
public class MutualExclusion {

	// Static informational variables
    public final static int RELEASED = 0;
    public final static int HELD = 1;
    public final static int WANTED = 2;
    public final static int BLOCKED = 3;
    public final static String[] requestStatus = new String[]{"RELEASED","HELD","WANTED","BLOCKED"};
    
    // Status variables
    private int state;
    private boolean voted;
    private int numRequests;
    private int numReplies;
    private int totalRequests;
    private int totalReplies;
    private int target;
    
    public MutualExclusion(int target) {
        this.state = RELEASED;
        this.voted = false;
        this.numRequests = 0;
        this.numReplies = 0;
        this.totalRequests = 0;
        this.totalReplies = 0;
        this.target = target;
    }
    
    // Update client status based on state variables
    public void updateStatus() {
    	if (checkReplyCount()) {
    		set_state(HELD);
        } else if (this.numReplies == 0 && this.numRequests == 0) {
        	set_state(RELEASED);
        } else if (!checkReplyCount() && this.numRequests > 0) {
        	set_state(BLOCKED);
        } else if (this.numReplies > 0 && this.numRequests == 0){
        	set_state(WANTED);
        }
    }
    
    // Check to see if received all ACKS / REPLY messages to REQUEST made
    public boolean checkReplyCount() {
    	return (this.numReplies == this.target);
    	//&& this.numRequests > 0
    }
    
    // Check to see if request should be queued
    public boolean checkStateAndVoted() {
    	return (this.state == HELD || this.voted);
    }
    
    // Reset counts of numReplies and update outstanding numRequests
    public void resetCounts() {
    	this.numReplies = 0;
    	this.numRequests--;
    }
    
    // Getters and Setters
    public int get_target() {
    	return target;
    }
    
    public void set_target(int target) {
    	this.target = target;
    }
    
    public int get_state() {
        return state;
    }
    
    public String get_stateString() {
        return requestStatus[state];
    }

    public void set_state(int state) {
        this.state = state;
    }
    
    public boolean get_voted() {
    	return voted;
    }
    
    public void set_voted(boolean voted) {
    	this.voted = voted;
    }
    
    public int get_numRequests() {
        return numRequests;
    }

    public void set_numRequests() {
        this.numRequests++;
        this.totalRequests++;
    }
    
    public int get_numReplies() {
        return numReplies;
    }

    public void set_numReplies() {
        this.numReplies++;
        this.totalReplies++;
    }
    
    public int get_totalRequests() {
        return totalRequests;
    }
    
    public int get_totalReplies() {
        return totalReplies;
    }
    
}
