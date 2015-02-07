public class Rule
{
	private String action;
	private String src = null;
	private String dest = null;
	private String kind = null;
	private Integer seqNum = null;
	private int matched = 0;
	public Rule(String action)
	{
		this.action = action;
	}

	public String get_action(){return action;}
	public String get_source(){return src;}
	public String get_destination(){return dest;}
	public String get_kind(){return kind;}
	public Integer get_seqNum(){return seqNum;}

	public void set_action(String action){this.action = action;}
	public void set_source(String src){this.src = src;}
	public void set_destination(String dest){this.dest = dest;}
	public void set_kind(String kind){this.kind = kind;}
	public void set_seqNum(Integer seqNum){this.seqNum = seqNum;}

	public String toString()
	{
		return  ("Action:" + action + "|Src:" + src + "|Dest:" + dest
				+ "|Kind:" + kind + "|seqNum:" + seqNum );
	}
	
	synchronized public void addMatch(){matched++;}
}


