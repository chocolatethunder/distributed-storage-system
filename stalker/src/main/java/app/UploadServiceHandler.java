package app;

import java.io.*;
import java.net.Socket;

/**
 *
 */
public class UploadServiceHandler implements Runnable {

    private final Socket socket;
    private DataInputStream in = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private String temp_dir = "temp/temp/";
    private String fileName;

    public UploadServiceHandler(Socket socket, String fname){
         this.socket = socket;
         this.fileName = fname;
    }

    @Override
    public void run() {

        byte[] chunkArray = new byte[1024];

        int bytesRead = 0;
        try {
            in = new DataInputStream(socket.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(temp_dir + fileName));
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


        }catch (IOException e){
            e.printStackTrace();
        }


//        FileChunker f = new FileChunker(chunk_dir);
//        ChunkDistributor cd = new ChunkDistributor(chunk_dir, harm_list);
//
//
//        //chunk file
//        IndexEntry entry = f.chunkFile(input_file, 3);
//        entry.summary();
//
//        //distribute file
//        if(cd.distributeChunks(entry, 3)){
//            entry.cleanLocalChunks();
//        }
//        //retrieve chunks
//        cr.retrieveChunks(entry);
//        //assemble the chunks
//        if(ca.assembleChunks(entry)){
//            System.out.println("Test passed without fail");
//        }


        //entry.summary();

    }
}
