package app.handlers;

import app.ChunkRetriever;
import app.chunk_utils.IndexEntry;
import app.chunk_utils.IndexFile;
import app.Request;

import java.net.Socket;

/**
 *This runnable class will handle requests for downloading a file
 */
public class DownloadServiceHandler implements Runnable {

    private final Socket socket;
    private String fileName;
    private IndexFile index;
    private final String c_dir = "temp/chunks/";
    private final String ass_dir = "temp/assembled/";

    public DownloadServiceHandler(Socket socket, Request req, IndexFile ind){
        this.socket = socket;
        this.fileName = req.getFileName();
        this.index = ind;
    }

    @Override
    public void run(){

        ChunkRetriever cr = new ChunkRetriever(c_dir);
        IndexEntry e = index.search(fileName);
        cr.retrieveChunks(e);
        ///used for download
//        //retrieve chunks

//        //assemble the chunks
//        if(ca.assembleChunks(entry)){
//            System.out.println("Test passed without fail");
//        }


        //entry.summary();
    }

}
