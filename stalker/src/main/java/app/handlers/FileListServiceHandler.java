package app.handlers;

import app.CommsHandler;
import app.MessageType;
import app.chunk_utils.IndexFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.Socket;
import java.util.List;

/**
 *This runnable class will handle request for retrieving filelist of all stored files in the
 * system
 */
public class FileListServiceHandler implements Runnable {

    private final Socket socket;
    private IndexFile index;
    private CommsHandler commLink;

    public FileListServiceHandler(Socket socket, IndexFile ind){
        this.socket = socket;
        this.index = ind;
        commLink = new CommsHandler();
    }





    @Override
    public void run(){
        List<String> l = index.fileList();
        ObjectMapper mapper = new ObjectMapper();
        String message = "";
        try{
            message = mapper.writeValueAsString(l);
            commLink.sendResponse(socket, MessageType.ACK);
            commLink.sendPacket(socket, MessageType.LIST, message, false);
            socket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
