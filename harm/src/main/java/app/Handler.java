package app;

import java.io.*;
import java.net.Socket;

/**
 *
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final RequestType requestType;
    private final String filename;


    public Handler(Socket socket, TcpPacket packet){
        this.socket = socket;
        this.requestType = RequestType.valueOf(packet.getRequestType());
        this.filename = packet.getFilename();

    }
    @Override
    public void run() {

        FileStreamer streamer = new FileStreamer(this.socket);

        if(requestType == RequestType.UPLOAD){
            streamer.sendFile(filename);
        }else if(requestType == RequestType.DOWNLOAD){
            streamer.receiveFile(filename);

        }else{
            //delete logic
        }



    }

}
