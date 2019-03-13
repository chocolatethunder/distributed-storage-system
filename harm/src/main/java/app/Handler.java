package app;

import java.io.*;
import java.net.Socket;

/**
 * This runnable handles request made by STALKERS
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final RequestType requestType;
    private final String filename;


    public Handler(Socket socket, TcpPacket packet) {
        this.socket = socket;
        this.requestType = RequestType.valueOf(packet.getRequestType());
        this.filename = packet.getFileName();

    }

    @Override
    public void run() {

        FileStreamer streamer = new FileStreamer(this.socket);

        // depending on the request type, it call appropriate method from streamer class
        if (requestType == RequestType.UPLOAD) {
            streamer.receiveFileFromSocket(filename);
        } else if (requestType == RequestType.DOWNLOAD) {
            streamer.sendFileToSocket(filename);

        } else {
            //delete logic here
        }

    }


}
