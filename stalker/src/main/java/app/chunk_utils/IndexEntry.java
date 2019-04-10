/*
 * Used to hold file metadata as well as a chunklist
 */
package app.chunk_utils;
import java.util.*;

public class IndexEntry {
    private String file_prefix;
    private String file_type;
    private long file_size;
    private int chunkCount;
    private List<Chunk> chunkList;
    //map a chunk index to a list of replicated chunks
    //private Map<int, List<String>> index_map;

    public IndexEntry(){}
    public IndexEntry(String file_prefix,String file_type, long file_size) {
        this.file_prefix = file_prefix;
        this.file_type = file_type;
        this.file_size = file_size;
        //index_map = new HashMap<int, List<String>>();
    }


    public void cleanLocalChunks() {
        for(Chunk c : chunkList){
            c.removeLocalChunk();
        }
    }
    /////-------------------------------------   getters/setters

    public String fileName(){ return(file_prefix + "." + file_type); }
    public long size(){return file_size;}
    public void setChunks(List<Chunk> chunks){
        chunkCount = chunks.size();
        chunkList = chunks;
    }

    public void addChunk(Chunk c){
        chunkList.add(c);
    }
    //print all data related to this entry
    public void summary(){
        System.out.println("Entry Summary: ");
        printMetaData();
        printChunkData();
        System.out.println("\n");
    }
    //print file info
    public void printMetaData(){
        System.out.println( "File name: " + file_prefix + ", File type: "
                           + file_type + ", File size: " + Integer.toString((int)file_size)
                           + ", Chunk count: " + Integer.toString((int)chunkCount));
    }
    //print data for each chunk
    public void printChunkData(){
        chunkList.forEach(c -> {System.out.println(c.toString()); c.printReplicas();});
    }

    public String clientData(){
        return("Filename: " + file_prefix + "; File Type: " + file_type + "; File Size: " + file_size);
    }

    public String getFile_prefix() { return file_prefix; }
    public void setFile_prefix(String file_prefix) { this.file_prefix = file_prefix; }
    public String getFile_type() { return file_type; }
    public void setFile_type(String file_type) { this.file_type = file_type; }
    public long getFile_size() { return file_size; }
    public void setFile_size(long file_size) { this.file_size = file_size; }
    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
    public List<Chunk> getChunkList() { return chunkList; }
    public void setChunkList(List<Chunk> chunkList) { this.chunkList = chunkList; }

}
