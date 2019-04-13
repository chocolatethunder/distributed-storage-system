package app;

import java.util.Set;

public class Request {

    private String fileName;
    private int fileSize;
    private MessageType requestType;
    private Set<String> harmAddresses;

    public Request(){}
    public Request(String fileName, MessageType r){
        this.fileName = fileName;
        this.requestType = r;
    }
    public Request(String fileName, MessageType r, int fileSize){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.requestType = r;
    }

    public Request(String fileName, MessageType r, Set<String> ips){
        this.fileName = fileName;
        this.requestType = r;
        this.harmAddresses = ips;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public MessageType getRequestType() {
        return requestType;
    }

    public void setRequestType(MessageType requestType) {
        this.requestType = requestType;
    }


    public Set<String> getHarmAddresses() {
        return harmAddresses;
    }

    public void setHarmAddresses( Set<String> ips) {
        this.harmAddresses = ips;
    }
}
