package app.handlers;

import app.*;
import app.chunk_utils.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This runnable class is responsible for handling delete file request
 */
public class DeleteServiceHandler implements Runnable {
    private ConfigFile cfg = ConfigManager.getCurrent();
    private final int server_port = cfg.getLeader_admin_port();
    private final Socket socket;
    private String fileName;
    private IndexFile index;
    private CommsHandler commsLink;
    @Override
    public void run() {
        Socket leader = null;
        try {
            Debugger.log("DeleteService: Starting delete process...", null);
            //0. make sure the file exists
            index = Indexer.loadFromFile();
            IndexEntry toRemove = index.search(fileName);
            if (toRemove == null) {
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DeleteService: Could not find entry");
            }
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if (!commsLink.sendRequestToLeader(MessageType.DELETE)) {
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DeleteService: Could not connect to leader.");
            }
            Debugger.log("DeleteService: Request sent to leader", null);


//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            leader = commsLink.getLeaderResponse(server_port);
            if (leader == null) {
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DeleteService: Error with leader connection");
            }
///------------------------------------------------------------

//         3. remove chunks
///------------------------------------------------------------
            if (!removeChunks(toRemove)) {
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "DeleteService: Could not remove chunks!");
            }
//          4. Send done status to leader
            commsLink.sendResponse(leader, MessageType.DONE);
            commsLink.sendPacket(leader, MessageType.DELETE, Indexer.serializeUpdate(new IndexUpdate(MessageType.DELETE, toRemove)), false);
            try {
                TcpPacket t = commsLink.receivePacket(leader);
                if (t.getMessageType() == MessageType.ACK) {
                    //we are done with the connection to the leader
                    //then update index by removing the entry

                    //Indexer.removeEntry(index, toRemove);
                    //Indexer.saveToFile(index);
                    Debugger.log("DeleteService: File removed from system!", null);
                    commsLink.sendResponse(socket, MessageType.ACK);
                    leader.close();
                }
            } catch (IOException e) {
                Debugger.log("", e);
            }
        } catch (RuntimeException e) {
            Debugger.log("", e);
            try {
                socket.close();
                leader.close();
            } catch (IOException ex) {
                Debugger.log("", ex);
            }
            return;
        }
    }

    public DeleteServiceHandler(Socket socket, Request req, IndexFile ind) {
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = ind;
        commsLink = new CommsHandler();
    }
    public boolean removeChunks(IndexEntry e) {
        int port = cfg.getHarm_listen();
        Map<Integer, NodeAttribute> m = NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path());
        for (Chunk c : e.getChunkList()) {
            for (Integer i : c.getReplicas()) {
                try {
                    Socket harmServer = NetworkUtils.createConnection(m.get(i).getAddress(), port);
                    //if everything went well then we can send the damn file
                    //send the packet to the harm target
                    if (commsLink.sendPacket(harmServer, MessageType.DELETE, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.DELETE), true) == MessageType.ACK) {
                        harmServer.close();
                    }
                } catch (IOException ex) {
                    Debugger.log("", ex);
                    return (false);
                }
            }
        }
        return (true);
    }


}
