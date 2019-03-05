/*
 * Used to hold file metadata as well as a chunklist
 */
package app;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class IndexEntry {

    private bool processed = false;
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
    /////-------------------------------------   getters/setters

    public bool processed(){return processed;}
    public void setChunks(List<Chunk> chunks){
        chunkCount = chunks.size();
        chunkList = chunks;
    }
    public void addChunk(Chunk c){
        chunkList.add(chunk);
    }

    public long size(){return file_size;}
    //print all data related to this entry
    public void summary(){
        printMetaData();
        printChunkData();
    }
    //print file info
    public void printMetaData(){
        System.out.println( "File name: " + file_prefix + ", File type: "
                           + file_type + ", File size: " + Integer.toString((int)file_size)
                           + ", Chunk count: " + Integer.toString((int)chunkCount));
    }
    //print data for each chunk
    public void printChunkData(){
        chunkList.forEach(c -> System.out.println(c.toString()));
    }
}
