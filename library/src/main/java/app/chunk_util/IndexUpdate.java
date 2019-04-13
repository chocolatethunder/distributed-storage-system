package app.chunk_util;
import app.MessageType;
import app.chunk_util.IndexEntry;

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
