package app;

import java.io.Serializable;

/**
 *
 */
public class TcpPacket {


    private String requestType;
    private String message;
    private String filename;

    // need default for Jackson
    public TcpPacket(){}

    public TcpPacket(RequestType requestType, String message){

        this.requestType = requestType.name();
        this.message =  message;
    }

    public TcpPacket(RequestType requestType, String message, String filename){

        this.requestType = requestType.name();
        this.message =  message;
        this.filename = filename;
    }

    public String getRequestType(){
        return this.requestType;
    }

    public String getMessage(){
        return this.message;
    }

    public String getFilename(){
        return  this.filename;
    }



}
