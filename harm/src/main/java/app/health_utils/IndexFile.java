package app.health_utils;

import java.util.HashMap;
import java.util.Map;

// =============================================
// Modified Stalker Unit's IndexFile logics
// Format of indexfile:
//  {
//      "GUID_1" : "Hash",
//      "GUID_2" : "Hash",
//      "GUID_3" : "Hash",
//      "GUID_4" : "Hash",
//      . . .
//  }
// =============================================
public class IndexFile {

    // { ChunkName(GUIDs), Hash }
    private Map<String,String> entries;
    public IndexFile(){
        entries = new HashMap<>();
    }


    public void add(String id, String hash){
        entries.put(id, hash);
    }

    public void remove(String id){
        entries.remove(id);
    }

    ////////////////////getter/setter

    public Map<String, String> getEntries() {return entries; }
    public void setEntries(Map<String, String> entries) { this.entries = entries; }


}
