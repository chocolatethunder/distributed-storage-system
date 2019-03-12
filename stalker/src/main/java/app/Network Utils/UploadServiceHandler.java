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

    public UploadServiceHandler(Socket socket){
         this.socket = socket;
    }

    @Override
    public void run() {

        byte[] chunkArray = new byte[1024];

        int bytesRead = 0;
        try {
            in = new DataInputStream(socket.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("temp.txt"));
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

    }
}
