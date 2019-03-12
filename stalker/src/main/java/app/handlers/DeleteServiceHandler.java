package app.handlers;

import java.net.Socket;

/**
 *
 */
public class DeleteServiceHandler implements Runnable {

    private final Socket socket;


    public DeleteServiceHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

    }
}
