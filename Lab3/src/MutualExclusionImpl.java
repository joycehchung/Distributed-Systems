import java.util.ArrayList;

public class MutualExclusionService {
	protected enum State {
		RELEASE, HELD, WANTED
	}

	protected enum MessageType {
		MUTEX_REQUEST, MUTEX_RELEASE, MUTEX_ACK
	}

	String processName;
	MessagePasser mp;
	// mcs;

	State state = State.RELEASE;
	boolean voted = false;

	Group votingGroup;
	boolean[] bitMapACK;



	public boolean requestMutex() throws Exception {
		String emptyString = "";
		state = State.WANTED;
		
		if (state == State.HELD) {
			return false;
		}

		for (int i = 0; i < bitMapACK.length; i++) {
			bitMapACK[i] = false;
		}

		TimestampedMessage m = new TimestampedMessage(emptyString,
				MessageType.MUTEX_REQUEST.toString(), emptyString);

		synchronized (this) {
			mcs.rMulticast(votingGroup.getGroupName(), m);

			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw e;
			}
			this.state = State.HELD;
		}
		
		return true;
	}


	// Add mutex to TimeStamp.java
	public boolean releaseMutex() {
		
		if (state != State.HELD) {
			return false;
		}
		String emptyString = "";
		state = State.RELEASED;
		TimestampedMessage m = new TimestampedMessage(emptyString,
				MessageType.MUTEX_RELEASE.toString(), emptyString);
		mcs.rMulticast(votingGroup.getGroupName(), m);
		
		return true;
	}

	protected synchronized void releaseHandler(MulticastMessage m) {
		m.printMutexHeaders();
		
		if (requestQueue.size() < 1) {
			voted = false;
		}
		else {
			MulticastMessage msg = requestQueue.remove(0);
			sendReply(msg);
			voted = true;
		}
		
	}


	//From MessagePasser
	private synchronized void initVotingGroup() throws Exception {
		ArrayList<Group> groups = mcs.getGroups();
		String str = "";
		for (Group group : groups) {
			if (group.getGroupName().equals("group_" + processName)) {
				this.votingGroup = group;
				str += ("Voting group for " + this.processName
						+ " set to : [" + this.votingGroup.getGroupName() + "]\n");
				str += group.print();
				Client.getMessageShower().setJTextGroupConfig(str);
				return;
			}
		}

		throw new Exception("Couldn't initialize voting group for "
				+ this.processName);
	}


}

