package app.handlers;

import app.*;
import app.chunk_utils.ChunkAssembler;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;

import java.io.IOException;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *This runnable class will handle requests for downloading a file
 */
public class DownloadServiceHandler implements Runnable {

    private ConfigFile cfg = ConfigManager.getCurrent();
    private final int server_port = cfg.getLeader_admin_port();
    private final Socket socket;
    private String fileName;
    private IndexFile index;
    private final String c_dir = "temp/chunks/";
    private final String ass_dir = "temp/reassembled/";

    public DownloadServiceHandler(Socket socket, Request req, IndexFile ind){
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = Indexer.loadFromFile();
    }

    @Override
    public void run(){
        CommsHandler commsLink = new CommsHandler();
        try {
            // 0. we are going to check to see if the file exists first...
            Debugger.log("DownloadService: Starting download process...", null);
            IndexEntry e = index.search(fileName);
            if (e == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DownloadService: File does not exist.");
            }
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if (!commsLink.sendRequestToLeader(MessageType.DOWNLOAD)) {
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DownloadService: Could not connect to leader.");
            }
            Debugger.log("DownloadService: Request sent to leader", null);
//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            Socket leader = commsLink.getLeaderResponse(server_port);
            if(leader == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DownloadService: Error with leader connection");
            }
///-----------------
//          3. Now we must get the files from the harm targets
///------------------------------------------------------------
            ChunkRetriever cr = new ChunkRetriever(c_dir);
            if(cr.retrieveChunks(e)){
                Debugger.log("DownloadService: Chunks retrieved OK!", null);
            }
            e.summary();
//          4. Now we must reassemble the file
///------------------------------------------------------------
            ChunkAssembler ca = new ChunkAssembler(c_dir, ass_dir);
            if(ca.assembleChunks(e)){
                Debugger.log("DownloadService: Chunks assembled", null);
            }
            else{
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DownloadService: Error reassembling chunks");
            }
//          5. send it to the client
///------------------------------------------------------------
            commsLink.sendResponse(socket, MessageType.ACK);
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.sendFileToSocket(ass_dir + e.fileName());
            File f = new File(ass_dir + e.fileName());
            f.delete();

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
            Debugger.log("", ex);
        }
        catch (IOException err){
            Debugger.log("", err);
        }

    }

}
