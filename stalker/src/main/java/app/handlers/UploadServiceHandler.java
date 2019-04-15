package app.handlers;

import app.*;
import app.chunk_util.IndexEntry;
import app.chunk_util.IndexFile;
import app.chunk_util.IndexUpdate;
import app.chunk_util.Indexer;
import app.chunk_utils.*;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import app.FileStreamer;

/**
 *
 */
public class UploadServiceHandler implements Runnable {

    private int server_port;
    private final Socket socket;
    private DataInputStream in = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private String temp_dir = "temp/toChunk/";
    private String chunk_dir = "temp/chunks/";
    private String filePath;
    private IndexFile index;

    public UploadServiceHandler(Socket socket, Request req, IndexFile ind){
         this.socket = socket;
         this.filePath = temp_dir + req.getFileName();
         this.index = Indexer.loadFromFile();;
    }

    @Override
    public void run() {
        Debugger.log("Upload Service: Started upload process...", null);
        CommsHandler commsLink = new CommsHandler();
        try{
            server_port = ConfigManager.getCurrent().getLeader_admin_port();
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if(!commsLink.sendRequestToLeader(MessageType.UPLOAD)){
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Upload Service: Could not connect to leader.");
            }
            Debugger.log("Upload Service: Request sent to leader", null);
//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            TcpPacket req;
            Socket leader = commsLink.getLeaderResponse(server_port);
            if(leader == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Upload Service: Error with leader connection");
            }
///------------------------------------------------------------

//          3. ACK request from JCP and perform download of file
///------------------------------------------------------------
            commsLink.sendResponse(socket, MessageType.ACK);
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFileFromSocket(filePath);
//            if (!getFileFromJCP()){
//                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Error when getting file from JCP.");
//            }
///------------------------------------------------------------
//          4. distribute to harm targets
            IndexEntry update = distributeToHarm();
            if(update == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Upload Service: Error when Distributing to Harm Target.");
            }
///------------------------------------------------------------
//          5. Send done status to leader
            commsLink.sendResponse(leader, MessageType.DONE);
            //send the update to the leader for distribution
            commsLink.sendPacket(leader, MessageType.UPLOAD, Indexer.serializeUpdate(new IndexUpdate(MessageType.UPLOAD, update)), false);
            try {
                //get an ack that tells us we are done
                TcpPacket t = commsLink.receivePacket(leader);
                if(t.getMessageType() == MessageType.ACK){
                    //we are done with the connection to the leader
                    //then update index
                    //updateIndex(update);
                    Debugger.log("Upload Service: Upload complete!", null);
                    leader.close();
                }
                else{
                    Debugger.log("There was a problem updating stalkers :(", null);
                    leader.close();
                    throw new RuntimeException(NetworkUtils.timeStamp(1) + "Upload Service: Error when Updating STALKERS.");
                }
            }
            catch(IOException e){
                Debugger.log("", e);
            }

        }
        catch(RuntimeException e){
            commsLink.sendResponse(socket, MessageType.BUSY);
//            try{ socket.close();}
//            catch(IOException ex){ Debugger.log("", ex); }
            Debugger.log("", e);
            return;
        }
        //send final completion ack to JCP
        //commsLink.sendResponse(socket, MessageType.ACK);

    }


    public IndexEntry distributeToHarm(){
        List<Integer> harm_list = getHarms(0);
        FileChunker f = new FileChunker(chunk_dir);
        ChunkDistributor cd = new ChunkDistributor(chunk_dir, harm_list);
        ///////////////////////chunk file and get the index entry object
        IndexEntry entry = f.chunkFile(filePath, ConfigManager.getCurrent().getChunk_count());
        if (entry != null){
            File file = new File(filePath);
            file.delete();
        }
        entry.summary();
        ////////////////distribute file
        if(cd.distributeChunks(entry, ConfigManager.getCurrent().getReplica_count())){
            entry.cleanLocalChunks();
            entry.summary();
            return entry;
        }
        else{
            return null;
        }
    }


    //in the future will update other harms
    public boolean updateIndex(IndexEntry entry){
        Indexer.addEntry(index, entry);
        /////////Save that shit
        Indexer.saveToFile(index);
        return true;
    }


    //get harm list by id
    public List<Integer>getHarms(int i){
        List<Integer> temp = new ArrayList<Integer>();
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getHarm_list_path()));
        for (Integer key : m.keySet()) {
            temp.add(key);
        }
        return temp;
    }

}
