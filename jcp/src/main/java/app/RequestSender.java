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
     * @param filePath absolute file path
     */
    public void sendFile(String filePath){
        MessageType m = MessageType.UPLOAD;
        if(NetworkUtils.checkFile(filePath)){
            String fileName = FilenameUtils.getName(filePath);
            //send a request and wait for ACK before proceeding
            if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m), true) == MessageType.ACK) {
                FileStreamer fileStreamer = new FileStreamer(socket);
                fileStreamer.sendFileToSocket(filePath);
            }else{
                //need a way to connect to another STALKER
                //DEBUG
                System.out.println(NetworkUtils.timeStamp(1) + "SERVER BUSY");
            }
        }
    }
    /**
     * This will delete a file with the filename in the system
     * @param fileName actual file name (not path)
     */
    public void deleteFile(String fileName){

        MessageType m = MessageType.DELETE;
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m), true) == MessageType.ACK) {
        }
    }
    /**
     * This will download a file given the filename
     *
     * @param filePath
     */
    public void getFile(String filePath){
        MessageType m = MessageType.DOWNLOAD;
        // TO:DO need logic to verify file size  here
        String fileName = FilenameUtils.getName(filePath);
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m), true) == MessageType.ACK) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFileFromSocket(filePath);

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
