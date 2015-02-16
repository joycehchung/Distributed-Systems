import java.util.*;
import java.util.concurrent.*;

public class Group {
	public String groupName;
	public int numMembers;
	public ArrayList<String> groupMembers;
	public ClockService groupVectorClock;
	public BlockingQueue<TimeStampedMessage> groupHoldQueue;
	
    public Group(String groupName, String[] nodes, String myName) {
        this.groupName = groupName;
        this.groupMembers = new ArrayList<String>();
        this.groupMembers.addAll(Arrays.asList(nodes));
        this.groupMembers.add(myName);
        this.numMembers = this.groupMembers.size();
        this.groupVectorClock = ClockService.CreateClockService(ClockService.ClockTypes.VECTOR, nodes, myName);
        this.groupHoldQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    }
	
}