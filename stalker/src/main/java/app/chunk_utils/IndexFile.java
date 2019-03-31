package app.chunk_utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class IndexFile {
    private Map<String,IndexEntry> entries;
    //maps uuid to mac id list
    private Map<String,List<String>> chunkIndex;
    public IndexFile(){
        entries = new HashMap<>();
        chunkIndex = new HashMap<>();
    }


    public void summary(){
        for(IndexEntry e : entries.values()){
            e.summary();
        }
    }

    public IndexEntry search(String filename){
        return(entries.get(filename));
    }

    //get the info to be sent
    public List<String> infoList(){
        List<String> temp = new ArrayList<>();
        for(IndexEntry e: entries.values()){
            temp.add(e.clientData());
        }
        return temp;
    }

    public void add(IndexEntry e){
        entries.put(e.fileName(), e);
    }
    public void indexChunks(IndexEntry e){
        for (Chunk c : e.getChunkList()){
            chunkIndex.put(c.getUuid(), c.getReplicas());
        }
    }
    ////////////////////getter/setter

    public Map<String, IndexEntry> getEntries() {return entries; }
    public void setEntries(Map<String, IndexEntry> entries) { this.entries = entries; }
    public Map<String, List<String>> getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Map<String, List<String>> chunkIndex) { this.chunkIndex = chunkIndex; }

}
