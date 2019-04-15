package app;
import java.io.*;
import java.net.Socket;


/**
 *
 */
public class FileStreamer {

    Socket socket = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    BufferedInputStream bufferedInputStream = null;

    public FileStreamer(Socket socket) {
        this.socket = socket;
    }

    public void sendFileToSocket(String filepath) {


        if (socket != null) {
            try {
                // send file
                File file = new File(filepath);
                byte[] byteArray = new byte[(int) file.length()];
                out = new DataOutputStream(socket.getOutputStream());
                bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(byteArray, 0, byteArray.length);
                Debugger.log("FileStreamer: Sending " + filepath + "(" + byteArray.length + " bytes)", null);
                out.write(byteArray, 0, byteArray.length);
                out.flush();
                Debugger.log("FileStreamer: Done.", null);
            } catch (IOException ex) {
                Debugger.log("FileStreamer: Could not retrieve file: ", ex);
            } finally {
                try {
                  bufferedInputStream.close();
                  //out.close();
                  //For debugging
                 // System.out.println(socket.isClosed());

                    // Close these in the calling method
                  // socket.close();
                } catch (Exception i) {
                    Debugger.log("", i);
                }
            }
        }

    }

    public void receiveFileFromSocket(String fileName) {
        if (socket != null) {
            BufferedOutputStream bufferedOutputStream = null;
            Debugger.log("FileStreamer: Retrieving file... " + fileName, null);
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
                Debugger.log("FileStreamer: Done.", null);

            } catch (IOException ex) {
                Debugger.log("", ex);
            } finally {
                try {
                    bufferedOutputStream.close();
                    out.close();

                    //Close this from calling method;
                   // socket.close();
                } catch (IOException i) {
                    Debugger.log("", i);
                }
            }
        }

    }
}

