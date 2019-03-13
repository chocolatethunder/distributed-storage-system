package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 */
public class FileStreamer {

    Socket socket = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    BufferedInputStream bufferedInputStream = null;

    FileStreamer(Socket socket) {
        this.socket = socket;
    }

    public void sendFileToSocket(String filepath) {


        if (socket != null) {
            try {

                // send file
                File file = new File(filepath);
                //HARD CODED????


                byte[] byteArray = new byte[(int) file.length()];

                out = new DataOutputStream(socket.getOutputStream());
                bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(byteArray, 0, byteArray.length);

                System.out.println("Sending " + filepath + "(" + byteArray.length + " bytes)");

                out.write(byteArray, 0, byteArray.length);
                out.flush();
                System.out.println("Done.");

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                  bufferedInputStream.close();
                  out.close();
                  //For debugging
                 // System.out.println(socket.isClosed());

                    // Close these in the calling method
                  // socket.close();
                } catch (Exception i) {
                    i.printStackTrace();
                }
            }
        }

    }

    public void receiveFileFromSocket(String fileName) {


        if (socket != null) {
            BufferedOutputStream bufferedOutputStream = null;
            try {

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                bufferedOutputStream = new BufferedOutputStream(
                        new FileOutputStream(fileName));

                byte[] chunkArray = new byte[1024];


                int bytesRead;
                while ((bytesRead = in.read(chunkArray)) != -1 )
                {
                    bufferedOutputStream.write(chunkArray, 0, bytesRead);

                }

                bufferedOutputStream.flush();
                System.out.println("Done.");

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    bufferedOutputStream.close();
                    out.close();

                    //Close this from calling method;
                   // socket.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        }

    }
}

