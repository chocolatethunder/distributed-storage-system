/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import app.chunk_util.Chunk;
import app.chunk_util.IndexEntry;

public class ChunkRetriever {
    private boolean debug = false;
    private String chunkDir;
    private CommsHandler commLink;
    //hardcoded test variables

    public ChunkRetriever(String c_dir) {
        chunkDir = c_dir;
        commLink = new CommsHandler();
    }


    public boolean retrieveChunks(IndexEntry iEnt){
        // each chunk indexd in the entry try and get a chunk from one of the
        //nodes that carries it
        for (Chunk c : iEnt.getChunkList()){
            if(!retrieveChunk(c)){
                return(false);
            }
        }
        return true;

    }

    //retreives a chunk from a node
    //if it cannot get any copies of the chunk it will fail and return false
    //in the future will retrieve it from a remote node
    public boolean retrieveChunk(Chunk c){
        //get the map of harm ids
        //HashMap<Integer, String > m = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        Map<Integer, NodeAttribute> n = NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path());
        int attempts = 0;
        for (Integer s : c.getReplicas()){
            NodeAttribute targ = n.get(s);
            while(true){
                //no pint in checking if target is dead
                if (!targ.isAlive()){
                    break;
                }
                if (attempts == 3){
                    Debugger.log("Chunk Retriever: Failed to receive file from HARM target after multiple attempts", null);
                    break;
                }
                try{
                    Socket harmServer = NetworkUtils.createConnection(targ.getAddress(), ConfigManager.getCurrent().getHarm_listen());
                    if(commLink.sendPacket(harmServer, MessageType.DOWNLOAD, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.DOWNLOAD), true) == MessageType.ACK){
                        FileStreamer fileStreamer = new FileStreamer(harmServer);
                        fileStreamer.receiveFileFromSocket(chunkDir + c.getUuid());
                        harmServer.close();
                        c.setChunk_path(chunkDir + c.getUuid());
                        return true;
                    }

                }
                catch(IOException e){
                    Debugger.log("Chunk Retriever: Attempt: " + attempts + " failed!", e);
                    attempts++;
                }
            }

        }
        return(false);
    }

}
