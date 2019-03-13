package app.chunk_utils;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class IndexFile {
    private Map<String,IndexEntry> entries;
    public IndexFile(){}


    public void summary(){
        for(IndexEntry e : entries.values()){
            e.summary();
        }
    }

    //get the info to be sent
    public List<String> infoList(){
        List<String> temp = new ArrayList<>();
        for(IndexEntry e: entries.values()){
            temp.add(e.getClientData());
        }
        return temp;
    }

    public void add(IndexEntry e){
        entries.put(e.fileName(), e);
    }


}
