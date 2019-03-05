package app;

import java.io.Serializable;

/**
 *
 */
public class TcpPacket {


    private String requestType;
    private String message;

    // need default for Jackson
    public TcpPacket(){}

    public TcpPacket(RequestType requestType, String message){

        this.requestType = requestType.name();
        this.message =  message;
    }

    public String getRequestType(){
        return this.requestType;
    }

    public String getMessage(){
        return this.message;
    }



}
