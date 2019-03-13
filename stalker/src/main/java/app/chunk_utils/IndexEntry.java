/*
 * Used to hold file metadata as well as a chunklist
 */
package app.chunk_utils;
import java.util.*;

public class IndexEntry {

    private boolean processed = false;
    private String file_prefix;
    private String file_type;
    private long file_size;
    private int chunkCount;
    private List<Chunk> chunkList;
    //map a chunk index to a list of replicated chunks
    //private Map<int, List<String>> index_map;

    public IndexEntry(){}
    public IndexEntry(String prefix,String type, long size) {
        file_prefix = prefix;
        file_type = type;
        file_size = size;
        //index_map = new HashMap<int, List<String>>();
    }


    public void cleanLocalChunks() {
        for(Chunk c : chunkList){
            c.removeLocalChunk();
        }
    }
    /////-------------------------------------   getters/setters

    public String fileName(){ return(file_prefix + "." + file_type); }
    public boolean processed(){return processed;}
    public void setChunks(List<Chunk> chunks){
        chunkCount = chunks.size();
        chunkList = chunks;
    }
    public void addChunk(Chunk c){
        chunkList.add(c);
    }

    public List<Chunk> getChunks(){
        return(chunkList);
    }

    public long size(){return file_size;}
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

    public String getClientData(){
        return("Filename: " + file_prefix + "; File Type: " + file_type + "; File Size: " + file_size);
    }
}
