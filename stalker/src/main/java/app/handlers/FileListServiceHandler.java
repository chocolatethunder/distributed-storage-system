package app.handlers;

import app.CommsHandler;
import app.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.chunk_utils.Indexer;
import java.net.Socket;
import java.util.List;

/**
 *This runnable class will handle request for retrieving filelist of all stored files in the
 * system
 */
public class FileListServiceHandler implements Runnable {

    private final Socket socket;
    private CommsHandler commLink;

    public FileListServiceHandler(Socket socket){
        this.socket = socket;
        commLink = new CommsHandler();
    }





    @Override
    public void run(){
        List<String> l = Indexer.fileList();
        System.out.println("ADADWDAWDWDWD");
        ObjectMapper mapper = new ObjectMapper();
        String message = "";
        try{
            message = mapper.writeValueAsString(l);
            commLink.sendResponse(socket, MessageType.ACK);
            commLink.sendPacket(socket, MessageType.LIST, message, false);
            //socket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
