package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * TCP packets will only be used to send content if the message type requires it
 *
 */

public class TcpPacket {


    private MessageType messageType;
    private String message;



    private String fileName;
    private int fileSize;

    // need default for Jackson
    public TcpPacket(){}

    public TcpPacket(MessageType requestType, String message){

        this.messageType = requestType;
        this.message =  message;
    }

    public MessageType getMessageType(){
        return this.messageType;
    }

    public String getMessage(){
        return this.message;
    }

    // used for file transfer operations
    // if we are going to send a file
    // we need to specify the name of the file and make sure it exists before we send it
    // we also need to make sure the file isn't too big!
    public void setFile(String fname, int size){

        fileName = fname;
        fileSize = size;
    }

    public String getFileName(){
        return fileName;
    }

    public int getFileSize(){
        return(fileSize);
    }



}
