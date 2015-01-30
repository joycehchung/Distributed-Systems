public class Rule
{
	private String action;
	private String src = null;
	private String dest = null;
	private String kind = null;


	public Rule(String action)
	{
		this.action = action;
	}

	public String getAction(){return action;}
	public String getSrc(){return src;}
	public String getDest(){return dest;}
	public String getKind(){return kind;}


	public void setAction(String action){this.action = action;}
	public void setSrc(String src){this.src = src;}
	public void setDest(String dest){this.dest = dest;}
	public void setKind(String kind){this.kind = kind;}


	synchronized public void addMatch(){matched++;}
	public void resetMatch(){matched = 0;}

	public String toString()
	{
		return  ("Action:" + action + "|Src:" + src + "|Dest:" + dest
				+ "|Kind:" + kind + "|ID:" + id + "|Nth:" + nth + "|EveryNth:"
				+ everynth + " |matched: " + matched + " time(s)");
	}
}

