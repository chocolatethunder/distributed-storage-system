/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import app.chunk_utils.Chunk;
import app.chunk_utils.IndexEntry;

public class ChunkRetriever {
    private boolean debug = false;
    private String chunkDir;
    private int port = 22222;
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
        Map<Integer, NodeAttribute> n = NetworkUtils.getNodeMap("config/harm.list");
        int attempts = 0;
        for (Integer s : c.getReplicas()){
            NodeAttribute targ = n.get(s);
            while(true){
                //no pint in checking if target is dead
                if (!targ.isAlive()){
                    break;
                }
                if (attempts == 3){
                    System.out.println("Failed to receive file from HARM target after multiple attempts");
                    break;
                }
                try{
                    Socket harmServer = NetworkUtils.createConnection(targ.getAddress(), 22222);
                    if(commLink.sendPacket(harmServer, MessageType.DOWNLOAD, NetworkUtils.createSerializedRequest(c.getUuid(), MessageType.DOWNLOAD), true) == MessageType.ACK){
                        FileStreamer fileStreamer = new FileStreamer(harmServer);
                        fileStreamer.receiveFileFromSocket(chunkDir + c.getUuid());
                        harmServer.close();
                        c.setChunk_path(chunkDir + c.getUuid());
                        return true;
                    }

                }
                catch(IOException e){
                    e.printStackTrace();
                    System.out.println("Attempt: " + attempts + " failed!");
                    attempts++;
                }
            }

        }
        return(false);
    }

}
