import java.io.*;


public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Message(String dest, String kind, Object data) {
		
	}
	
	// These settors are used by MessagePasser.send, not your app
	public void set_source(String source) {
		
	}
	
	public void set_seqNum(int sequenceNumber) {
		
	}
	
	public void set_duplicate(Boolean dupe) {
		
	}
	
	// Other accessors, toString etc. as needed
}
