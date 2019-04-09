package app.handlers;

import app.*;
import app.chunk_utils.FileChunker;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.ServerSocket;
import java.util.HashMap;
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
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not connect to leader.");
            }
            System.out.println("Request sent to leader");
//          2. Wait for Leader to grant job permission
///------------------------------------------------------------
            TcpPacket req;
            Socket leader;
            try{
                listener = new ServerSocket(server_port);
                leader = listener.accept();
                System.out.println(NetworkUtils.timeStamp(1) + "Connected to leader: ");
                req = commsLink.receivePacket(leader);
                System.out.println(NetworkUtils.timeStamp(1) + "Permission from leader granted");
            }
            catch (IOException e){
                e.printStackTrace();
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Could not use server port");
            }
            if (!(req.getMessageType() == MessageType.START)){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Request denied by leader.");
            }
///------------------------------------------------------------

//          3. ACK request from JCP and perform download of file
///------------------------------------------------------------
            commsLink.sendResponse(socket, MessageType.ACK);
            if (!getFileFromJCP()){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Error when getting file from JCP.");
            }
///------------------------------------------------------------
//          4. distribute to harm targets
            IndexEntry update = distributeToHarm();
            if(update == null){
                throw new RuntimeException(NetworkUtils.timeStamp(1) + "Error when Distributing to Harm Target.");
            }
///------------------------------------------------------------
//          5. Send done status to leader
            commsLink.sendResponse(leader, MessageType.DONE);

            try {
                if(commsLink.receivePacket(leader).getMessageType() == MessageType.ACK){
                    //we are done with the connection to the leader
                    leader.close();
                    //then update index
                    updateIndex(update);
                }
            }
            catch(IOException e){

            }
        }
        catch(RuntimeException e){
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

    public IndexEntry distributeToHarm(){
        List<String> harm_list = getHarms(0);
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


    //temp hard code function
    public List<String>getHarms(){
        List<String> temp = new ArrayList<String>();
        temp.add("192.168.1.131");
        temp.add("192.168.1.107");
        temp.add("192.168.1.146");
        return temp;
    }

    //temp hard code function
    public List<String>getHarms(int i){
        List<String> temp = new ArrayList<String>();
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        for (Integer key : m.keySet()) {
            temp.add(m.get(key));
        }
        return temp;
    }

}
