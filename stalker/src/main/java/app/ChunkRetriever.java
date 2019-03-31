/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app;
import java.io.*;
import java.net.Socket;

import app.chunk_utils.Chunk;
import app.chunk_utils.IndexEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunkRetriever {
    private boolean debug = false;
    private String chunkDir;
    //hardcoded test variables

    public ChunkRetriever(String c_dir) {
        chunkDir = c_dir;
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
        int attempts = 0;
        for (String s : c.getReplicas()){
            while(true){
                if (attempts == 3){
                    System.out.println("Failed to receive file from HARM target after multiple attempts");
                    break;
                }
                try{
                    System.out.println("SOCKET: " + Integer.valueOf(s));
                    int port = Integer.valueOf(s);
                    Socket harmServer = NetworkUtils.createConnection("127.0.0.1", port);
                    //FileUtils.copyFile(new File(s),new File(chunkDir + c.getHash()));
                    if(handShakeSuccess(MessageType.DOWNLOAD, c.getHash(), harmServer)){
                        FileStreamer fileStreamer = new FileStreamer(harmServer);
                        fileStreamer.receiveFileFromSocket(chunkDir + c.getHash());
                        harmServer.close();
                        return true;
                    }
                    c.setChunk_path(chunkDir + c.getHash());
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

    //sepcialized request for getting a file
    private boolean handShakeSuccess(MessageType requestType, String toGet, Socket socket){
        TcpPacket receivedPacket = null;
        try {

            TcpPacket initialPacket = new TcpPacket(requestType, "HELLO_INIT");
            //initialPacket.setFile(toGet, 0);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();
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
