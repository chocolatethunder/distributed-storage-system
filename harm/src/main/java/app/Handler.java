package app;

import app.health_utils.HashIndex;
import app.health_utils.HashIndexer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.Socket;
import java.io.File;

/**
 * This runnable handles request made by STALKERS
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final MessageType requestType;
    private final Request request;
    private String storage_path;
    private CommsHandler commLink;
    private String temp_path;

    public Handler(Socket socket, TcpPacket packet, int harm_id) {
        this.socket = socket;
        this.requestType = packet.getMessageType();
        this.request = NetworkUtils.getPacketContents(packet);
        this.storage_path = "storage/";
        this.temp_path = "temp/";

        commLink = new CommsHandler();
    }

    @Override
    public void run() {

        FileStreamer streamer = new FileStreamer(this.socket);

        // depending on the request type, it call appropriate method from streamer class
        if (requestType == MessageType.UPLOAD) {
            commLink.sendResponse(socket, MessageType.ACK);
            String temp_file = temp_path + request.getFileName();
            String file_path = storage_path + request.getFileName();
            streamer.receiveFileFromSocket(temp_file);
            try{
                FileUtils.moveFile(new File(temp_file), new File(file_path));
            }
            catch (IOException e){
                Debugger.log("Could not copy file", null);
            }
            // creating the index file to store the chunk information
            HashIndex hashIndex = HashIndexer.loadFromFile();
            hashIndex.add(request.getFileName(), request.getFileHash());
            HashIndexer.saveToFile(hashIndex);


        } else if (requestType == MessageType.DOWNLOAD) {
            commLink.sendResponse(socket, MessageType.ACK);
            streamer.sendFileToSocket(storage_path + request.getFileName());
        }
        else if (requestType == MessageType.DELETE) {
            File f = new File(storage_path + request.getFileName());
            f.delete();
            HashIndex hashIndex = HashIndexer.loadFromFile();
            hashIndex.remove(storage_path + request.getFileName());
            HashIndexer.saveToFile(hashIndex);
            commLink.sendResponse(socket, MessageType.ACK);
        }
        else if (requestType == MessageType.REPLACE) {
            File f = new File(storage_path + request.getFileName());
            f.delete();
            commLink.sendResponse(socket, MessageType.ACK);
            streamer.receiveFileFromSocket(storage_path + request.getFileName());
            //delete logic here
            Debugger.log("Chunk replaced without fail!", null);
        }
        else{
            Debugger.log("invalid request", null);
        }
    }


}
