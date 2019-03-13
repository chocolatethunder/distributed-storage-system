package app.handlers;

import app.chunk_utils.IndexFile;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.net.Socket;

/**
 *This runnable class will handle requests for downloading a file
 */
public class DownloadServiceHandler implements Runnable {

    private final Socket socket;
    private String fileName;
    private IndexFile index;

    public DownloadServiceHandler(Socket socket, String fname, IndexFile ind){
        this.socket = socket;
        this.fileName = fname;
        this.index = ind;
    }

    @Override
    public void run(){




        
        ///used for download
//        //retrieve chunks
//        cr.retrieveChunks(entry);
//        //assemble the chunks
//        if(ca.assembleChunks(entry)){
//            System.out.println("Test passed without fail");
//        }


        //entry.summary();
    }

}
