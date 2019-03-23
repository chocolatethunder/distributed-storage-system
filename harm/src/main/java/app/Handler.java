package app;

import java.net.Socket;

/**
 * This runnable handles request made by STALKERS
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final MessageType requestType;
    private final String filename;
    private String storage_path;

    public Handler(Socket socket, TcpPacket packet, int harm_id) {
        this.socket = socket;
        this.requestType = MessageType.valueOf(packet.getRequestType());
        this.filename = packet.getFileName();
        this.storage_path = "../../storage/";
    }

    @Override
    public void run() {

        FileStreamer streamer = new FileStreamer(this.socket);

        // depending on the request type, it call appropriate method from streamer class
        if (requestType == MessageType.UPLOAD) {
            streamer.receiveFileFromSocket(storage_path + filename);
        } else if (requestType == MessageType.DOWNLOAD) {
            streamer.sendFileToSocket(storage_path + filename);

        } else {
            //delete logic here
        }

    }


}
