
import java.util.Map;
public class LogicalTimeStamp extends TimeStamp {


    private int time;

    public LogicalTimeStamp(LogicalTimeStamp lts){
        this.time = lts.time;
    }

    public LogicalTimeStamp(int time){
        this.time = time;
    }

    @Override
    public int compareTo(TimeStamp ts){
        if (ts instanceof LogicalTimeStamp){
            LogicalTimeStamp lts = (LogicalTimeStamp) ts;
            if (this.time > lts.time)
            return 1;
        else if (lts.time < this.time)
            return -1;
        else
            return 0;
        }else{
            System.out.println("Invalid TimeStamp!");
            return -2;
        }
    }

    public int getLogicalTime(){
        return time;
    }

    public void setTime(int time){
        this.time = Math.max(time, this.time);
    }

        @Override
        public String toString() {
                return "LogicalTimeStamp:" + time;
        }

        @Override
        public Map<String, Integer> getVectorTime() {
                // TODO Auto-generated method stub
                return null;
        }



}
