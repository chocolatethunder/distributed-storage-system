package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.commons.io.*;

/**
 *
 */
public class RequestSender {

    Socket socket = null;
    DataOutputStream out = null;

    private static class RequestSenderHolder{

        static final RequestSender requestSender = new RequestSender();
    }
    private RequestSender(){

        try {

            //TO:DO  need to implement the ROUND ROBIN logic to chose the STALKER to connect

            socket = createConnection("127.0.0.1", 6553);
        } catch (IOException e) {
            //Could not connect , need another STALKER here
        }

    }

    public static RequestSender getInstance(){
        return  RequestSenderHolder.requestSender;
    }



    public void sendFile(String fileName){

        // TO:DO need logic to verify file size here
        // we make the handshake first
        if(handShakeSuccess(RequestType.UPLOAD, fileName)) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.sendFile(fileName);

        }else{
            //need a way to connect to another STALKER
            //DEBUG
            System.out.println("SERVER BUSY");
        }
    }


    public List<String> getFileList(){


        if(handShakeSuccess(RequestType.LIST)) {

        }
        return null;
    }

    public void deleteFile(String fileName){

        // TO:DO need logic to verify file size  here

        if(handShakeSuccess(RequestType.DELETE)) {
        }
    }
    public void getFile(String filePath){
        // TO:DO need logic to verify file size  here
        if(handShakeSuccess(RequestType.DOWNLOAD)) {

            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFile(filePath);
        }
    }
    //just incase no file is specified
    private boolean handShakeSuccess(RequestType requestType){
        return(handShakeSuccess(requestType, ""));
    }

    //resposnible for making a request to the STALKER
    private boolean handShakeSuccess(RequestType requestType, String toSend){
        TcpPacket receivedPacket = null;
        try {

            TcpPacket initialPacket = new TcpPacket(requestType, "HELLO_INIT");
            // in the case that we are sending a file we need to also
            // send the name of the file as well as the file size
            //The request will not be sent if the file doesn't exist...
            File f = new File(toSend);
            if (f.exists()){
                initialPacket.setFile(FilenameUtils.getName(toSend), (int) f.length());
            }
            else{
                throw new FileNotFoundException("The file specified does not exist!");
            }


            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();

            //DEBUG : Object to JSON in file
            //mapper.writeValue(new File("file.json"), initialPacket);

            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);


            try {

                String received = in.readUTF();
                System.out.println("rec " + received);
                receivedPacket = mapper.readValue(received, TcpPacket.class);

            } catch (EOFException e) {
                // do nothing end of packet
            }

        } catch (IOException  e) {
            e.printStackTrace();
        }
        return receivedPacket != null && receivedPacket.getMessage().equals("AVAIL");
    }


    private Socket createConnection(String host, int port) throws IOException {
        Socket socket = null;

        // establish a connection
        //TO:DO Need logic for getting the stalker in round robin fashion
            socket = new Socket(host, port);
            System.out.println("Connected");
        return socket;
    }



}
