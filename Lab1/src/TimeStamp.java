import java.util.Map;

public abstract class TimeStamp {
    public String ts;
    public abstract int compareTo(TimeStamp ts);
    public abstract Map<String, Integer> getVectorTime();
    public abstract int getLogicalTime();
}
