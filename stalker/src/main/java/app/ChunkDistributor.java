/*
 * This Class will be responsible for replicating and distributing the chunks
 * among the harm targets
 */
package app;
import java.io.*;
import java.net.Socket;
import java.util.*;

import app.chunk_utils.Chunk;
import app.chunk_utils.IndexEntry;

public class ChunkDistributor {
    private boolean debug = false;
    private String chunkDir;
    private CommsHandler commLink;
    private int port = 22222;

    //we are going to hard code this for now
    private int harm_count = 4;
    //for now this will be a set of directories of "harm targets"
    //in the future these will be some sort of address
    List<Integer> harm_list;
    //hardcoded test variables
    public ChunkDistributor(String c_dir, List<Integer> h_list) {
        chunkDir = c_dir;
        //this list will contain harm id and ip in the future...
        harm_list = h_list;
        harm_count = h_list.size();
        commLink = new CommsHandler();
    }

    //take an index entry object and process distribute
    //will use round robin for now
    public boolean distributeChunks(IndexEntry iEnt, int num_reps) {
        //we go through each chunk in the IndexEntry object
        //token represents which harm target we are currently sending to

        int token = 0;
        for (Chunk c : iEnt.getChunkList()){
            //for each replica
            for (int i = 0; i < num_reps; i++){
                //get target ip from harm list
                Integer target_path = harm_list.get(token);
                //String target_ip = "127.0.0.1";

                if(sendChunk(c, target_path)) {
                    //add the address to the replica list if OK
                    //for now the port number is the identifier
                    c.addReplica(target_path);
                    token++;
                    //round robin that bitch
                    if (token == harm_count) { token = 0; }
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
    public boolean sendChunk(Chunk c, Integer target){
        HashMap<Integer, String > m = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        int attempts = 0;
        while(true){
            if (attempts == 3){
                System.out.println("Failed to send file to HARM target after multiple attempts");
                return false;
            }
            try{
                //old way
                //FileUtils.copyFile(new File(c.path()),new File(target));
                System.out.println("Sending chunk");
                //make a connection to the harm target
                Socket harmServer = NetworkUtils.createConnection(m.get(target), port);
                //if everything went well then we can send the damn file
                //send the packet to the harm target
                if(commLink.sendPacket(harmServer, MessageType.UPLOAD, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.UPLOAD), true) == MessageType.ACK){
                    FileStreamer fileStreamer = new FileStreamer(harmServer);
                    fileStreamer.sendFileToSocket(c.getChunk_path());
                    harmServer.close();
                    break;
                }
                attempts++;

                // filestream.sendFileToSocket()
            }
            catch(IOException e){
                System.out.println("Attempt: " + attempts + " failed!");
                //e.printStackTrace();
                try{
                    Thread.sleep((long)(Math.random() * 1000));
                }
                catch (InterruptedException ex){
                    ex.printStackTrace();
                }
                attempts++;
            }
        }


        return(true);
    }

    public void debug() { debug = !debug; }

}
