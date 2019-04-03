package app.chunk_utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class IndexFile {
    private Map<String,IndexEntry> entries;
    //maps uuid to mac id list
    private Map<String,List<Integer>> chunkIndex;
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
        return(entries.get(UUID.nameUUIDFromBytes(filename.getBytes()).toString()));
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
        entries.put(UUID.nameUUIDFromBytes(e.fileName().getBytes()).toString(), e);
    }
    public void indexChunks(IndexEntry e){
        for (Chunk c : e.getChunkList()){
            chunkIndex.put(c.getUuid(), c.getReplicas());
        }
    }
    ////////////////////getter/setter

    public Map<String, IndexEntry> getEntries() {return entries; }
    public void setEntries(Map<String, IndexEntry> entries) { this.entries = entries; }
    public Map<String, List<Integer>> getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Map<String, List<Integer>> chunkIndex) { this.chunkIndex = chunkIndex; }

}


