package app.handlers;

import app.*;
import app.chunk_utils.Chunk;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 *This runnable class is responsible for handling delete file request
 */
public class DeleteServiceHandler implements Runnable {
    private final int server_port = 11113;
    private final Socket socket;
    private String fileName;
    private IndexFile index;
    private CommsHandler commsLink;

    public DeleteServiceHandler(Socket socket, Request req, IndexFile ind){
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = ind;
        commsLink = new CommsHandler();
    }

    @Override
    public void run(){
        Socket leader = null;
        try{
            //0. make sure the file exists
            IndexEntry toRemove = index.search(fileName);
            if(toRemove == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not find entry");
            }
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if(!commsLink.sendRequestToLeader(MessageType.DELETE)){
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not connect to leader.");
            }
            System.out.println("Request sent to leader");

//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            System.out.println("Debug0");
            leader = commsLink.getLeaderResponse(server_port);
            if(leader == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Error with leader connection");
            }
///------------------------------------------------------------

           System.out.println("Debug1");
//         3. remove chunks
///------------------------------------------------------------
            if(!removeChunks(toRemove)){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not remove chunks!");
            }
            System.out.println("Debug2");
//          4. Send done status to leader
            commsLink.sendResponse(leader, MessageType.DONE);
            try {
                TcpPacket t = commsLink.receivePacket(leader);
                if(t.getMessageType() == MessageType.ACK){
                    //we are done with the connection to the leader
                    //then update index by removing the entry
                    Indexer.removeEntry(index, toRemove);
                    Indexer.saveToFile(index);
                    System.out.println("File removed from system!");
                    leader.close();
                }
            }
            catch(IOException e){
                System.out.println("Debug3");
            }
        }
        catch(RuntimeException e){
            try{ socket.close(); leader.close();}
            catch(IOException ex){ ex.printStackTrace(); }
            e.printStackTrace();
            return;
        }
    }


    public boolean removeChunks(IndexEntry e){
        int port = 22222;
        HashMap<Integer, String > m = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        for (Chunk c : e.getChunkList()){
            for (Integer i : c.getReplicas()){
                try{
                    Socket harmServer = NetworkUtils.createConnection(m.get(i), port);
                    //if everything went well then we can send the damn file
                    //send the packet to the harm target
                    if(commsLink.sendPacket(harmServer, MessageType.DELETE, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.DELETE), true) == MessageType.ACK){
                        harmServer.close();
                    }
                }
                catch (IOException ex){
                    ex.printStackTrace();
                    return(false);
                }
            }
        }
        return(true);
    }



}
