package app.handlers;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.net.Socket;

/**
 *This runnable class will handle requests for downloading a file
 */
public class DownloadServiceHandler implements Runnable {

    private final Socket socket;
    private String fileName;

    public DownloadServiceHandler(Socket socket, String fname){
        this.socket = socket;
        this.fileName = fname;
    }

    @Override
    public void run(){

    }

}
