
public class DemoA {
	public static MessagePasser mp;
	public static ClockService cs;
	
    public static void main(String[] args) {
    	
    	mp = new MessagePasser("config","george");
    	
    	// Application code can access ClockService outside of MessagePasser
    	cs = mp.clockService;

    }
}
