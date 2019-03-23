package app.handlers;

import app.*;
import app.chunk_utils.FileChunker;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;
import java.net.ServerSocket;
import java.net.ServerSocket;
import java.util.List;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 */
public class UploadServiceHandler implements Runnable {

    private final int server_port = 11113;
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
         this.index = ind;
    }

    @Override
    public void run() {


        CommsHandler commsLink = new CommsHandler();
        ServerSocket listener;
        try{
//          1. get permissions from leader
//------------------------------------------------------------
            //going to need IP of leader
            if(!commsLink.sendRequestToLeader(MessageType.UPLOAD)){
                commsLink.sendResponse(socket, MessageType.ERROR);
                throw new IOException("Could not connect to leader.");
            }
//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            listener = new ServerSocket(server_port);
            Socket leader = listener.accept();
            TcpPacket req = commsLink.receivePacket(leader);
            if (!(req.getMessageType() == MessageType.START)){
                
            }
///------------------------------------------------------------


//          3. ACK request from JCP and perform download of file
///------------------------------------------------------------
            commsLink.sendResponse(socket, MessageType.ACK);
            if (!getFileFromJCP()){
                throw new IOException("Error when getting file from JCP.");
            }
///------------------------------------------------------------
            //////////////////////////////File Chunking section
//          3. distribute to harm targets
            if(!distributeToHarm()){
                throw new IOException("Error when Distributing to Harm Target.");
            }



        }
        catch(IOException e){
            try{
                socket.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
            return;
        }

        //       3. confirm completion
        /////////////////////////
        /////////////////////////
        // UPDATE REMAINING STALKERS
    }


    public boolean getFileFromJCP(){
        byte[] chunkArray = new byte[1024];
        int bytesRead = 0;
        try {
            in = new DataInputStream(socket.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            while ((bytesRead = in.read(chunkArray)) != -1) {

                bufferedOutputStream.write(chunkArray, 0, bytesRead);
                bufferedOutputStream.flush();
                System.out.println("File "
                        + " downloaded (" + bytesRead + " bytes read)");
            }
            bufferedOutputStream.close();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean distributeToHarm(){
        List<String> harm_list = getHarms();
        FileChunker f = new FileChunker(chunk_dir);
        ChunkDistributor cd = new ChunkDistributor(chunk_dir, harm_list);
        ///////////////////////chunk file and get the index entry object
        IndexEntry entry = f.chunkFile(filePath, 3);
        if (entry != null){
            File file = new File(filePath);
            file.delete();
        }
        entry.summary();
        ////////////////distribute file
        if(cd.distributeChunks(entry, 3)){
            entry.cleanLocalChunks();
            entry.summary();
            Indexer.addEntry(index, entry);
            /////////Save that shit
            Indexer.saveToFile(index);
        }
        else{
            return false;
        }
        return true;
    }


    public List<String>getHarms(){
        List<String> temp = new ArrayList<String>();
        temp.add("192.168.1.131");
        temp.add("192.168.1.107");
        temp.add("192.168.1.146");
        return temp;
    }

}
