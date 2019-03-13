package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;
import org.apache.commons.io.*;

/**
 *This is the singleton object that will do the round robin connection of
 * STALKER unit and make requests to STALKER for download, upload, delete and filelist
 */
public class RequestSender {

    Socket socket = null;
    DataOutputStream out = null;
    NetworkUtils networkUtils = null;


    /**
     * Single Instance of RequestSender Holder
     */
    private static class RequestSenderHolder{

        static final RequestSender requestSender = new RequestSender();
    }


    private RequestSender(){
        networkUtils = new NetworkUtils();
    }


    public static RequestSender getInstance(){
        return  RequestSenderHolder.requestSender;
    }


    /**
     * This is the method that connects to a given host and port
     *
     */
    public void connect(String host, int port){
        try {

            //TO:DO modify this to connect to STALKER in a round robin fashion

            socket = networkUtils.createConnection(host, port);
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
        // we make the handshake first
        if(handShakeSuccess(RequestType.UPLOAD, fileName)) {
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
    //just incase no file is specified


    /**
     * This is the handshake logic between JCP and STALKER
     * Occurs in 2 steps  HELLO_INIT -> AVAIL | BUSY
     * @param requestType  UPLOAD,
     *                     DOWNLOAD,
     *                     DELETE,
     *                     LIST
     * @return  true if handshake was successfull, false otherwise
     */
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

                // receiving packet back from STALKER
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
