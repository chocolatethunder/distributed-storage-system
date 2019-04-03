package app.handlers;

import app.*;
import app.chunk_utils.ChunkAssembler;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *This runnable class will handle requests for downloading a file
 */
public class DownloadServiceHandler implements Runnable {

    private final int server_port = 11113;
    private final Socket socket;
    private String fileName;
    private IndexFile index;
    private final String c_dir = "temp/chunks/";
    private final String ass_dir = "temp/reassembled/";

    public DownloadServiceHandler(Socket socket, Request req, IndexFile ind){
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = ind;
    }

    @Override
    public void run(){
        CommsHandler commsLink = new CommsHandler();
        try {
            // 0. we are going to check to see if the file exists first...
            IndexEntry e = index.search(fileName);
            if (e == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "File does not exist.");
            }
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if (!commsLink.sendRequestToLeader(MessageType.DOWNLOAD)) {
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not connect to leader.");
            }
            System.out.println("Request sent to leader");
//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            Socket leader = commsLink.getLeaderResponse(server_port);
            if(leader == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Error with leader connection");
            }
///-----------------
//          3. Now we must get the files from the harm targets
///------------------------------------------------------------
            ChunkRetriever cr = new ChunkRetriever(c_dir);
            if(cr.retrieveChunks(e)){
                System.out.println("Chunks retrieved OK!");
            }
//          4. Now we must reassemble the file
///------------------------------------------------------------
            ChunkAssembler ca = new ChunkAssembler(c_dir, ass_dir);
            if(ca.assembleChunks(e)){
                System.out.println("Chunks assembled");
            }
//          5. send it to the client
///------------------------------------------------------------
            commsLink.sendResponse(socket, MessageType.ACK);
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.sendFileToSocket(ass_dir + e.fileName());

            ///file is sent
//          6.tell the leader you are done
///------------------------------------------------------------
            commsLink.sendResponse(leader, MessageType.DONE);
            TcpPacket t = commsLink.receivePacket(leader);
            if (t.getMessageType() == MessageType.ACK) {
                //we are done with the connection to the leader
                leader.close();
            }
        }
        catch (RuntimeException ex){
            ex.printStackTrace();
        }
        catch (IOException err){
            err.printStackTrace();
        }














        ///used for download
//        //retrieve chunks

//        //assemble the chunks
//        if(ca.assembleChunks(entry)){
//            System.out.println("Test passed without fail");
//        }


        //entry.summary();
    }

}
