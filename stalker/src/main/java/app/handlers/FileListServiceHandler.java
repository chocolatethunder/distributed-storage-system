package app.handlers;

import java.net.Socket;

/**
 *
 */
public class FileListServiceHandler implements Runnable {

    private final Socket socket;


    public FileListServiceHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

    }
}
