package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 *This is the singleton object that will do the round robin connection of
 * STALKER unit and make requests
 */
public class RequestSender {

    Socket socket = null;
    DataOutputStream out = null;
    NetworkUtils networkUtils = null;



    private static class RequestSenderHolder{

        static final RequestSender requestSender = new RequestSender();
    }


    private RequestSender(){
        networkUtils = new NetworkUtils();
    }


    public static RequestSender getInstance(){
        return  RequestSenderHolder.requestSender;
    }


    public void connect(){
        try {
            socket = networkUtils.createConnection("127.0.0.1", 6555);
        } catch (IOException e) {
            //Could not connect , need another STALKER here
        }
    }


    /**
     * This is the request for uploading file
     *
     * @param fileName absolute file path
     */
    public void sendFile(String fileName){

        // TO:DO need logic to verify file size here

        if(handShakeSuccess(RequestType.UPLOAD)) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.sendFileToSocket(fileName);

        }else{
            //need a way to connect to another STALKER
            //DEBUG
            System.out.println("SERVER BUSY");
        }

    }


    /**
     * This will fetch a list of all file names in the system
     * @return list of filenames
     */
    public List<String> getFileList(){


        if(handShakeSuccess(RequestType.LIST)) {



        }

        return null;

    }


    /**
     * This will delete a file with the filename in the system
     * @param fileName actual file name (not path)
     */
    public void deleteFile(String fileName){

        // TO:DO need logic to verify file size  here

        if(handShakeSuccess(RequestType.DELETE)) {



        }

    }


    /**
     * This will download a file given the filename
     *
     * @param filePath
     */
    public void getFile(String filePath){

        // TO:DO need logic to verify file size  here

        if(handShakeSuccess(RequestType.DOWNLOAD)) {

            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFileFromSocket(filePath);

        }

    }


    /**
     * This is the handshake logic between JCP and STALKER
     * Occurs in 2 steps  HELLO_INIT -> AVAIL | BUSY
     * @param requestType
     * @return
     */
    private boolean handShakeSuccess(RequestType requestType){
        TcpPacket receivedPacket = null;
        try {


            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));

            TcpPacket initialPacket = new TcpPacket(requestType, "HELLO_INIT", "something");

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

}
