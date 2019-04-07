package app.chunk_utils;
import app.MessageType;

public class IndexUpdate {


    private MessageType mt;
    private IndexEntry entry;
    public IndexUpdate(){}
    public IndexUpdate(MessageType m, IndexEntry e){
        mt = m;
        entry = e;
    }

    public MessageType getMt() { return mt; }
    public void setMt(MessageType mt) { this.mt = mt; }
    public IndexEntry getEntry() { return entry; }
    public void setEntry(IndexEntry entry) { this.entry = entry; }



}
