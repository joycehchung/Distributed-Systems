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
        this.numMembers = nodes.length + 1;
        this.groupMembers = new ArrayList<String>();
        this.groupMembers.addAll(Arrays.asList(nodes));
        this.groupVectorClock = ClockService.CreateClockService(ClockService.ClockTypes.VECTOR, nodes, myName);
        this.groupHoldQueue = new LinkedBlockingQueue<TimeStampedMessage>();
    }
	
}