/*
 * This Class will be responsible for replicating and distributing the chunks
 * among the harm targets
 */
package app;
import java.io.*;
import java.net.Socket;
import java.util.*;

import app.FileStreamer;
import app.NetworkUtils;
import app.RequestType;
import app.TcpPacket;
import app.chunk_utils.Chunk;
import app.chunk_utils.IndexEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;

public class ChunkDistributor {
    private boolean debug = false;
    private String chunkDir;

    //we are going to hard code this for now
    private int harm_count = 4;

    //for now this will be a set of directories of "harm targets"
    //in the future these will be some sort of address
    List<String> harm_list;
    //hardcoded test variables
    public ChunkDistributor(String c_dir, List<String> h_list) {
        chunkDir = c_dir;
        //this list will contain harm id and ip in the future...
        harm_list = h_list;
        harm_count = h_list.size();
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
                String target_path = harm_list.get(token);
                //String target_ip = "127.0.0.1";

                if(sendChunk(c, target_path)) {
                    //add the address to the replica list if success
                    //for now the port number is the identifier
                    c.addReplica("" + (6665 + token));
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
    public boolean sendChunk(Chunk c, String target){
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
                //make a connection to the harm target using the port modifier
                NetworkUtils networkUtils = new NetworkUtils();
                Socket harmServer = networkUtils.createConnection("127.0.0.1", 22222);
                //if everything went well then we can send the damn file
                if(handShakeSuccess(RequestType.UPLOAD, c.getChunk_path(), harmServer)){
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
                attempts++;
            }
        }


        return(true);
    }



    //YEAH I KNOW MORE REUSED CODE
    //WILL CLEAN IN THE FUTURE
    //resposnible for making a request to the STALKER
    private boolean handShakeSuccess(RequestType requestType, String toSend, Socket socket){
        TcpPacket receivedPacket = null;
        try {

            TcpPacket initialPacket = new TcpPacket(requestType, "HELLO_INIT");
            // in the case that we are sending a file we need to also
            // send the name of the file as well as the file size
            //The request will not be sent if the file doesn't exist...
            File f = new File(toSend);
            if (f.exists()){
                initialPacket.setFile(FilenameUtils.getName(toSend), (int) f.length());
            }
            else{
                throw new FileNotFoundException("The file specified does not exist!");
            }
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();

            //DEBUG : Object to JSON in file
            //mapper.writeValue(new File("file.json"), initialPacket);

            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);


            try {

                // receiving packet back from STALKER
                String received = in.readUTF();
                System.out.println("rec " + received);
                receivedPacket = mapper.readValue(received, TcpPacket.class);

            } catch (EOFException e) {
                // do nothing end of packet
            }

        } catch (IOException  e) {
            e.printStackTrace();
        }
        return receivedPacket != null && receivedPacket.getMessage().equals("AVAIL");
    }



    public void debug() { debug = !debug; }

}
