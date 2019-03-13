package app.handlers;

import java.net.Socket;

/**
 *This runnable class is responsible for handling delete file request
 */
public class DeleteServiceHandler implements Runnable {

    private final Socket socket;
    private String fileName;

    public DeleteServiceHandler(Socket socket, String fname){
        this.socket = socket;
        this.fileName = fname;
    }

    @Override
    public void run(){

    }
}
