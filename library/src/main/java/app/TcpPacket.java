package app;

import java.io.Serializable;

/**
 *
 */
public class TcpPacket {

    public enum RequestType{
        UPLOAD,
        DOWNLOAD,
        LIST
    }

    private String requestType;
    private String message;

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
