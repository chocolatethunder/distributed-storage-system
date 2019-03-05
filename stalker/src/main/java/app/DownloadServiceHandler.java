package app;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.net.Socket;

/**
 *
 */
public class DownloadServiceHandler implements Runnable {

    private final Socket socket;


    public DownloadServiceHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

    }

}
