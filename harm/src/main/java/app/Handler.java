package app;

import app.health_utils.hashIndex;
import app.health_utils.HashIndexer;

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

    public Handler(Socket socket, TcpPacket packet, int harm_id) {
        this.socket = socket;
        this.requestType = packet.getMessageType();
        this.request = NetworkUtils.getPacketContents(packet);
        this.storage_path = "storage/";
        commLink = new CommsHandler();
    }

    @Override
    public void run() {

        FileStreamer streamer = new FileStreamer(this.socket);

        // depending on the request type, it call appropriate method from streamer class
        if (requestType == MessageType.UPLOAD) {
            commLink.sendResponse(socket, MessageType.ACK);
            streamer.receiveFileFromSocket(storage_path + request.getFileName());


            // creating the index file to store the chunk information
            hashIndex hashIndex = new hashIndex();
            hashIndex.add(request.getFileName(), request.getFileHash());
            HashIndexer.saveToFile(hashIndex);


        } else if (requestType == MessageType.DOWNLOAD) {
            commLink.sendResponse(socket, MessageType.ACK);
            streamer.sendFileToSocket(storage_path + request.getFileName());
        }
        else if (requestType == MessageType.DELETE) {
            File f = new File(storage_path + request.getFileName());
            f.delete();
            hashIndex hashIndex = new hashIndex();
            hashIndex.remove(request.getFileName());
            HashIndexer.saveToFile(hashIndex);
            commLink.sendResponse(socket, MessageType.ACK);
        }
        else {
            //delete logic here
        }
    }


}
