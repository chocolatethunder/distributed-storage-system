package app;

public class Request {

    private String fileName;
    private int fileSize;
    private MessageType requestType;
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











}
