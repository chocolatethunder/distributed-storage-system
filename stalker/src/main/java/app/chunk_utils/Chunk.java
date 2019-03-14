/*
 * Holds the chunk metadata for each chunk
 */
package app.chunk_utils;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;
import java.security.MessageDigest;

public class Chunk {
    //private String chunkStatus;
    private String hash;
    //the index is stored for easy reassembly
    private int chunk_index;
    //holds the path to a chunk if it exists in local storage space, otherwise null
    private String chunk_path;
    private long chunk_size;

    //this list will contain the adresses of each of the chunk replicas that
    // are hosted on then harm targets. will probably become an object in the future
    private List<String> replicas;

    public Chunk(){ }

    public Chunk(String c_path, int c_index, long c_size){
        //temporary hash function for filenames
        String toHash = c_path + Integer.toString(c_index);
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(toHash.getBytes());
            hash = messageDigest.digest().toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        this.chunk_path = c_path + hash;
        this.chunk_index = c_index;
        this.chunk_size = c_size;
        this.replicas = new ArrayList();
    }


    public boolean removeLocalChunk(){
        File f = new File(chunk_path);
        f.delete();
        chunk_path = "";
        return true;
    }


    //add a replica to the tracked list
    public void addReplica(String addr){replicas.add(addr);}

    @Override
    public String toString(){
        return("Chunk: " + Integer.toString(chunk_index) +", size: "
                + Integer.toString((int)chunk_size) + "b, hash: " + hash
                + ", Local Path: " + chunk_path);
    }
    public void printReplicas(){
        System.out.println("Replicas: ");
        int i = 0;
        for (String s : replicas){
            System.out.println(i + " ----> " + s);
            i++;
        }
    }


    //////////////////////////////////-----------------------  Getter/Setters
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public int getChunk_index() { return chunk_index; }
    public void setChunk_index(int chunk_index) { this.chunk_index = chunk_index; }
    public String getChunk_path() { return chunk_path; }
    public void setChunk_path(String chunk_path) { this.chunk_path = chunk_path; }
    public long getChunk_size() { return chunk_size; }
    public void setChunk_size(long chunk_size) { this.chunk_size = chunk_size; }
    public List<String> getReplicas() { return replicas; }

    public void setReplicas(List<String> replicas) { this.replicas = replicas; }
}
