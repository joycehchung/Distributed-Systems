import java.io.*;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private int seqNum;
    private String src;
    private String dest;
    private String kind;
    private Object data;

    public Message(String dest, String kind, Object data) {
        this.dest = dest;
        this.kind = kind;
        this.data = data;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }



    @Override
    public String toString() {
        return "From:" + this.getSrc() + " to:" + this.getDest() +
               " Seq:" + this.getSeqNum() + " Kind:" + this.getKind()
               + " Data:" + this.getData();
    }
}

