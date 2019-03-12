/*
 * This Class will be responsible for replicating and distributing the chunks
 * among the harm targets
 */
package app;
import java.io.*;
import java.net.Socket;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class ChunkDistributor {
    private boolean debug = false;
    private String chunkDir;

    //for now this will be a set of directories of "harm targets"
    //in the future these will be some sort of address
    List<String> harm_list;
    //hardcoded test variables
    public ChunkDistributor(String c_dir, List<String> h_list) {
        chunkDir = c_dir;
        harm_list = h_list;
    }

    //take an index entry object and process distribute
    //will use round robin for now
    public boolean distributeChunks(IndexEntry iEnt, int num_reps) {
        //we go through each chunk in the IndexEntry object
        //token represents which harm target we are currently sending to
        int token = 0;
        for (Chunk c : iEnt.getChunks()){
            //for each replica
            for (int i = 0; i < num_reps; i++){
                String target_path = harm_list.get(token) + c.hash();
                if(sendChunk(c, target_path)) {
                    //add the address to the replica list if success
                    c.addReplica(target_path);
                    token++;
                    //round robin that bitch
                    if (token == harm_list.size()) { token = 0; }
                }
                else {
                    //for now we are just going to fail
                    // (future) what to with other sent chunks if this fails??
                    System.out.println("Sending chunk failed!");
                    return false;
                }
            }

        }
        return true;
    }

    //placeholder chunk sending function
    public boolean sendChunk(Chunk c, String target){

        try{
            //copy the file to the target directory
            //in the future this will instead copy the file to a byte stream
            //and get a response from the node
            FileUtils.copyFile(new File(c.path()),new File(target));

            NetworkUtils networkUtils = new NetworkUtils();
            Socket harmServer = networkUtils.createConnection("127.0.0.1", 7555);

            //handshake
            // filestream.sendFileToSocket()


        }
        catch(IOException e){
            System.out.println("Error copying chunk to directory: ");
            e.printStackTrace();
            return(false);
        }
        return(true);
    }

    public void debug() { debug = !debug; }

}
