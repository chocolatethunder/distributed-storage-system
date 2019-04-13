package app;

public class Request {

    private String fileName;
    private int fileSize;
    private MessageType requestType;
    private String fileHash;
    public Request(){}
    public Request(String fileName, MessageType r){
        this.fileName = fileName;
        this.requestType = r;
    }
    public Request(String fileName, MessageType r, int fileSize, String fileHash){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.requestType = r;
        this.fileHash = fileHash;
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

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
}
