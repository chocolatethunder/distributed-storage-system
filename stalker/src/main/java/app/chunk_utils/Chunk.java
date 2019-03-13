/*
 * Holds the chunk metadata for each chunk
 */
package app.chunk_utils;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;
import java.security.MessageDigest;

public class Chunk {
    private String chunkStatus;
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
        chunk_path = c_path + hash;
        chunk_index = c_index;
        chunk_size = c_size;
        replicas = new ArrayList();
    }


    public boolean removeLocalChunk(){
        File f = new File(chunk_path);
        f.delete();
        chunk_path = "";
        return true;
    }


    //-----------------------  Getter/Setters
    public int index(){return chunk_index;}
    public long size(){return chunk_size;}
    public String path(){return chunk_path;}
    public String hash(){return hash;}
    public List<String> getReplicaAddrs(){return(replicas);}


    //add a replica to the tracked list
    public void addReplica(String addr){replicas.add(addr);}
    public void setPath(String new_path){chunk_path = new_path;}

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


}
