package app.handlers;

import app.chunk_utils.FileChunker;
import app.chunk_utils.IndexEntry;
import app.ChunkDistributor;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;

import java.util.List;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 */
public class UploadServiceHandler implements Runnable {

    private final Socket socket;
    private DataInputStream in = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private String temp_dir = "temp/toChunk/";
    private String chunk_dir = "temp/chunks/";
    private String filePath;
    private IndexFile index;

    public UploadServiceHandler(Socket socket, String fname, IndexFile ind){
         this.socket = socket;
         this.filePath = temp_dir + fname;
         this.index = ind;
    }

    @Override
    public void run() {

        byte[] chunkArray = new byte[1024];

        int bytesRead = 0;
        try {
            in = new DataInputStream(socket.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            while ((bytesRead = in.read(chunkArray)) != -1) {

                bufferedOutputStream.write(chunkArray, 0, bytesRead);
                bufferedOutputStream.flush();
                System.out.println("File "
                        + " downloaded (" + bytesRead + " bytes read)");
            }
            bufferedOutputStream.close();


        }catch (IOException e){
            e.printStackTrace();
        }
        List<String> harm_list = new ArrayList<String>();
        FileChunker f = new FileChunker(chunk_dir);
        ChunkDistributor cd = new ChunkDistributor(chunk_dir, harm_list);

//        //chunk file and get the index entry object
        IndexEntry entry = f.chunkFile(filePath, 3);
        if (entry != null){
            File file = new File(filePath);
            file.delete();
        }
        entry.summary();
//
        //distribute file
        if(cd.distributeChunks(entry, 3)){
            entry.cleanLocalChunks();
        }
        entry.summary();

        Indexer.addEntry(index, entry);

        Indexer.saveToFile(index);
    }
}
