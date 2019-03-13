package app.handlers;

import java.net.Socket;

/**
 *This runnable class will handle request for retrieving filelist of all stored files in the
 * system
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
