
public class Demo {
	public static MessagePasser mp;
	public static ClockService cs;
	
    public static void main(String[] args) {
    	
    	mp = new MessagePasser(args[0], args[1]);
    	
    	// Application code can access ClockService outside of MessagePasser
    	cs = mp.clockService;

    }
}
