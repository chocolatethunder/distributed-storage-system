package app.handlers;

import app.chunk_utils.IndexFile;
import app.Request;
import java.net.Socket;

/**
 *This runnable class is responsible for handling delete file request
 */
public class DeleteServiceHandler implements Runnable {

    private final Socket socket;
    private String fileName;
    private IndexFile index;

    public DeleteServiceHandler(Socket socket, Request req, IndexFile ind){
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = ind;
    }

    @Override
    public void run(){

    }
}
