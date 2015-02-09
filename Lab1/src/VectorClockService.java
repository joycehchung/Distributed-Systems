

import java.util.ArrayList;
public class VectorClockService extends ClockService {

	private VectorTimeStamp vectorts;
	private String name;

	public VectorClockService(ArrayList<String> hosts, String name) {
		this.vectorts = new VectorTimeStamp(hosts);
		this.name = name;
	}

	public VectorClockService(VectorClockService vcs_){
		this.vectorts = new VectorTimeStamp((VectorTimeStamp)vcs_.getTimeStamp());
		this.name = new String(vcs_.getName());
	}

	public String getName(){
		return this.name;
	}

	public TimeStamp increaseTS() {
		this.vectorts.setTime(this.name, this.vectorts.getTime(name) + 1);
		return new VectorTimeStamp(vectorts);
	}

	public void updateTS(TimeStamp ts) {
		if (ts instanceof VectorTimeStamp) {
			VectorTimeStamp new_vts = (VectorTimeStamp) ts;
			this.vectorts.setTime(new_vts);
			this.increaseTS();
		} else {
			System.out.println("TimeStamp is invalid.");
		}
	}

	public TimeStamp getTimeStamp() {
		return vectorts;
	}
}
