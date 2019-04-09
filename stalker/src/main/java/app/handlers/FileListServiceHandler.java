package app.handlers;

import app.chunk_utils.IndexFile;

import java.net.Socket;

/**
 *This runnable class will handle request for retrieving filelist of all stored files in the
 * system
 */
public class FileListServiceHandler implements Runnable {

    private final Socket socket;
    private IndexFile index;

    public FileListServiceHandler(Socket socket, IndexFile ind){
        this.socket = socket;
        this.index = ind;
    }

    @Override
    public void run(){

    }
}
