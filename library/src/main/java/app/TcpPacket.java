package app;

import java.io.Serializable;

/**
 *
 */
public class TcpPacket {


    private String requestType;
    private String message;
    private string fileName;
    private int fileSize;

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

    // used for file tranfer operations
    // if we are going to send a file
    // we need to specify the name of the file and make sure it exists before we send it
    // we also need to make sure the file isn't too big!
    public boolean setFile(String fname){
        try{
            File file = new File(fname);
            fileName = fname;
            fileSize = file.length();
        }
        catch (IOException e){
            return false;
        }

    }



}
