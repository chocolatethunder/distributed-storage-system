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
    CommsHandler commLink;
    /**
     * Single Instance of RequestSender Holder
     */
    private static class RequestSenderHolder{

        static final RequestSender requestSender = new RequestSender();
    }
    private RequestSender(){
        commLink = new CommsHandler();
    }
    public static RequestSender getInstance(){
        return  RequestSenderHolder.requestSender;
    }

    /**
     * This is the method that connects to a given host and port
     *
     */
    public Socket connect(String host, int port){
        try {

            //TO:DO modify this to connect to STALKER in a round robin fashion
            this.socket = NetworkUtils.createConnection(host, port);
        } catch (IOException e) {
            //Could not connect , need another STALKER here
        }
        return this.socket;
    }
    /**
     * This is the request for uploading file
     *
     * @param fileName absolute file path
     */
    public void sendFile(String fileName){
        MessageType m = MessageType.UPLOAD;
        if(NetworkUtils.checkFile(fileName)){
            //send a request and wait for ACK before proceeding
            if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m)) == MessageType.ACK) {
                FileStreamer fileStreamer = new FileStreamer(socket);
                fileStreamer.sendFileToSocket(fileName);
            }else{
                //need a way to connect to another STALKER
                //DEBUG
                System.out.println("SERVER BUSY");
            }
        }
    }
    /**
     * This will delete a file with the filename in the system
     * @param fileName actual file name (not path)
     */
    public void deleteFile(String fileName){

        MessageType m = MessageType.DELETE;
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m)) == MessageType.ACK) {
        }
    }
    /**
     * This will download a file given the filename
     *
     * @param fileName
     */
    public void getFile(String fileName){
        MessageType m = MessageType.DOWNLOAD;
        // TO:DO need logic to verify file size  here
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m)) == MessageType.ACK) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFileFromSocket(fileName);

        }

    }

    //    /**
//     * This will fetch a list of all file names in the system
//     * @return list of filenames
//     */
//    public List<String> getFileList(){
//
//
//        if(handShakeSuccess(MessageType.LIST)) {
//
//        }
//        return null;
//    }
}
