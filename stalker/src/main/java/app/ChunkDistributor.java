/*
 * This Class will be responsible for replicating and distributing the chunks
 * among the harm targets
 */
package app;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Random;
import app.chunk_util.Chunk;
import app.chunk_util.IndexEntry;

public class ChunkDistributor {
    private boolean debug = false;
    private String chunkDir;
    private CommsHandler commLink;
    private int port;

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
        port = ConfigManager.getCurrent().getHarm_listen();
    }

    //take an index entry object and process distribute
    //will use round robin for now
    public boolean distributeChunks(IndexEntry iEnt, int num_reps) {
        //we go through each chunk in the IndexEntry object
        //token represents which harm target we are currently sending to
        Random rand = new Random();
        int token = rand.nextInt(harm_count);
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
                    Debugger.log("Chunk Distributor: Sending chunk failed!", null);
                    return false;
                }
            }

        }
        return true;
    }

    //placeholder chunk sending function
    public boolean sendChunk(Chunk c, Integer target){
        Map<Integer, NodeAttribute> n = NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path());
        //HashMap<Integer, String > m = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        int attempts = 0;
        while(true){
            if (attempts == 3){
                Debugger.log("Chunk Distributor: Failed to send file to HARM target after multiple attempts", null);
                return false;
            }
            try{
                NodeAttribute targ = n.get(target);
                //old way
                //FileUtils.copyFile(new File(c.path()),new File(target));
                Debugger.log("Chunk Distributor: Sending chunk", null);

                //make sure the harm meets the requirements of the chunk size
//                if (targ.getSpace() < c.getChunk_size()){
//                    throw new RuntimeException("Chunk Distributor: Harm server at address: " + targ.getAddress() + " does not have sufficient space: required: " + c.getChunk_size() + " available: " + targ.getSpace());
//                }
//                else if (!targ.isAlive()){
//                    throw new RuntimeException("Chunk Distributor: Harm server at address: " + targ.getAddress() + " is not responding");
//                }
                //make a connection to the harm target
                Socket harmServer = NetworkUtils.createConnection(targ.getAddress(), port);
                //if everything went well then we can send the damn file
                //send the packet to the harm target
                if(commLink.sendPacket(harmServer, MessageType.UPLOAD, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.UPLOAD, createDigest(c.getChunk_path())), true) == MessageType.ACK){
                    FileStreamer fileStreamer = new FileStreamer(harmServer);
                    fileStreamer.sendFileToSocket(c.getChunk_path());
                    harmServer.close();
                    break;
                }
                attempts++;

                // filestream.sendFileToSocket()
            }
            catch(IOException e){
                Debugger.log("", e);
                Debugger.log("Chunk Distributor: Attempt: " + attempts + " failed!", null);
                //e.printStackTrace();
                try{
                    Thread.sleep((long)(Math.random() * 1000));
                }
                catch (InterruptedException ex){
                    Debugger.log("", ex);
                }
                attempts++;
            }
            catch (RuntimeException ex){
                Debugger.log("", ex);
                break;
            }
        }


        return(true);
    }

    public void debug() { debug = !debug; }
    public static String createDigest(String uuid){
        // Opening File
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // shouldn't arrive here
            e.printStackTrace();
        }


        try (InputStream is = Files.newInputStream(Paths.get(uuid));
             DigestInputStream dis = new DigestInputStream(is, md)) {
            int i;
            while ((i = dis.read()) != -1){
                // read through the file & update digest
            }
        }
        catch (IOException e){
            //if the file is corrupt or empty, report as corrupt
            e.printStackTrace();
            System.out.println(uuid + " is inaccessible.");

        }
        // Check digest
        String hash = md.digest().toString();

        return hash;
    }
}
