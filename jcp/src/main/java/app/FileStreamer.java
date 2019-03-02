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
    BufferedInputStream bufferedInputStream = null;

    public void sendFile(String fileName) {


        socket = createConnection("127.0.0.1", 6553);

        if(socket != null){

            if(handShakeSuccess()){
                try {

                    // send file
                    File file = new File(fileName);

                    byte[] byteArray = new byte[(int) file.length()];

                    bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    bufferedInputStream.read(byteArray, 0, byteArray.length);

                    System.out.println("Sending " + fileName + "(" + byteArray.length + " bytes)");

                    out.write(byteArray, 0, byteArray.length);
                    out.flush();
                    System.out.println("Done.");

                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        bufferedInputStream.close();
                        out.close();
                        socket.close();
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                }
            }else{
                System.out.println("Server was busy");
            }
        }

    }

    private boolean handShakeSuccess(){
        TcpPacket receivedPacket = null;
        try {


            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));

            TcpPacket initialPacket = new TcpPacket(TcpPacket.RequestType.UPLOAD, "HELLO INIT");

            ObjectMapper mapper = new ObjectMapper();
            //Object to JSON in file
            mapper.writeValue(new File("file.json"), initialPacket);

            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);


            try {

                String received = in.readUTF();
                System.out.println("rev " + received);
                receivedPacket = mapper.readValue(received, TcpPacket.class);

            } catch (EOFException e) {
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }

        return receivedPacket != null && receivedPacket.getMessage().equals("AVAIL");
    }


    private Socket createConnection(String host, int port){
        Socket socket = null;
        // establish a connection
        try {
            socket = new Socket(host, port);
            System.out.println("Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }
}

