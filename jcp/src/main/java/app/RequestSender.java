package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
            //try to close the socket first if it hasn't already
            NetworkUtils.closeSocket(this.socket);
            //TO:DO modify this to connect to STALKER in a round robin fashion
            this.socket = NetworkUtils.createConnection(host, port);
        } catch (IOException e) {
            Debugger.log("", e);
            return null;
            //Could not connect , need another STALKER here
        }
        return this.socket;
    }
    /**
     * This is the request for uploading file
     *
     * @param filePath absolute file path
     */
    public boolean sendFile(String filePath){
        MessageType m = MessageType.UPLOAD;
        if(NetworkUtils.checkFile(filePath)){
            String fileName = FilenameUtils.getName(filePath);
            //send a request and wait for ACK before proceeding
            if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m, ""), true) == MessageType.ACK) {
                FileStreamer fileStreamer = new FileStreamer(socket);
                fileStreamer.sendFileToSocket(filePath);

                if (getCompletion()){
                    Debugger.log("No problems during upload.", null);
                }
                else{
                    Debugger.log("Problems during upload.", null);
                }
                NetworkUtils.closeSocket(socket);
                return(true);
            }else{
                //need a way to connect to another STALKER
                //DEBUG
                Debugger.log("Error uploading file, please make sure HARM targets are connected properly...", null);
                NetworkUtils.closeSocket(socket);
                return(false);
            }

        }
        NetworkUtils.closeSocket(socket);
        return(false);
    }
    /**
     * This will delete a file with the filename in the system
     * @param fileName actual file name (not path)
     */
    public boolean deleteFile(String fileName){

        MessageType m = MessageType.DELETE;
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m, ""), true) == MessageType.ACK) {
            NetworkUtils.closeSocket(socket);
            return(true);
        }
        else{
            Debugger.log("Error during deletion process, please make sure HARM targets are connected properly...", null);
            NetworkUtils.closeSocket(socket);
            return(false);
        }

    }
    /**
     * This will download a file given the filename
     *
     * @param filePath
     */
    public boolean getFile(String filePath){
        MessageType m = MessageType.DOWNLOAD;
        // TO:DO need logic to verify file size  here
        String fileName = FilenameUtils.getName(filePath);
        if(commLink.sendPacket(socket, m, NetworkUtils.createSerializedRequest(fileName, m, ""), true) == MessageType.ACK) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFileFromSocket(filePath);
            if (getCompletion()){
                Debugger.log("No problems during download", null);
            }
            else{
                Debugger.log("Error during download", null);
            }

            NetworkUtils.closeSocket(socket);
            return(true);
        }
        else{
            Debugger.log("Error during Download process, please make sure HARM targets are connected properly...", null);
            NetworkUtils.closeSocket(socket);
            return(false);
        }


    }



    public boolean getCompletion(){

        try{socket.setSoTimeout(10000);}catch (SocketException  e){}
        MessageType m = commLink.receivePacket(socket).getMessageType();
        if (m == MessageType.ACK){
            return(true);
        }
        else{
            return(false);
        }
    }

    //    /**
//     * This will fetch a list of all file names in the system
//     * @return list of filenames
//     */
    public List<String> getFileList(){

        List<String> files = null;
        MessageType m = MessageType.LIST;
        if(commLink.sendPacket(socket, m, "", true) == MessageType.ACK) {
            TcpPacket t =  commLink.receivePacket(socket);
            files = NetworkUtils.listFromJson(t.getMessage());
        }
        else{
            Debugger.log("Error during getting file list...", null);
        }
        NetworkUtils.closeSocket(socket);
        return(files);
    }
}
