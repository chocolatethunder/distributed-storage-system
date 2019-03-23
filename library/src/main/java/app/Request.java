package app;

public class Request {

    private String fileName;
    private int fileSize;
    private String requestType;
    public Request(String fileName){
        this.fileName = fileName;
    }
    public Request(String fileName, int fileSize){
        this.fileName = fileName;
        this.fileSize = fileSize;
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
    public String getRequestType() {
        return requestType;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }






}
