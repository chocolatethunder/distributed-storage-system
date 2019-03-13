package app.handlers;

import java.net.Socket;

/**
 *
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
