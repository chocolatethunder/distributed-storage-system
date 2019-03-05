/*
 * Holds the chunk metadata for each chunk
 */
package app;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class Chunk {
    //the index is stored for easy reassembly
    private int chunk_index;
    //holds the path to a chunk if it exists in local storage space, otherwise null
    private String chunk_path;
    private long chunk_size;

    //this list will contain the adresses of each of the chunk replicas that
    // are hosted on then harm targets. will probably become an object in the future
    private List<String> chunk_addrs;

    public Chunk(){ }

    public Chunk(String c_path, int c_index, long c_size){
        chunk_path = c_path;
        chunk_index = c_index;
        chunk_size = c_size;
    }

    public int index(){return chunk_index;}
    public long size(){return chunk_size;}
    public String path(){return chunk_path;}

    @Override
    public String toString(){
        return("Chunk: " + Integer.toString(chunk_index) +", size: " + Integer.toString((int)chunk_size) + "b, Path: " + chunk_path);
    }

}
