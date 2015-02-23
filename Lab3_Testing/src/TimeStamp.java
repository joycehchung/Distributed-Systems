import java.io.Serializable;


@SuppressWarnings("serial")
public class TimeStamp implements Serializable {
	
	public String ts;
	
    public TimeStamp(String aString) {
    	this.ts = aString;
    }
    
}