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

}
