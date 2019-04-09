package app.LeaderUtils;

import app.TcpPacket;
import app.MessageType;
import java.net.InetAddress;

public class QueueEntry {
    private TcpPacket req;

    private InetAddress inetAddr;
    public QueueEntry(TcpPacket t, InetAddress s){
        req = t;
        inetAddr = s;
    }

    public InetAddress getWorker() {
        return inetAddr;
    }

    public TcpPacket getReq() {
        return req;
    }
    public MessageType getMessageType(){
        return(req.getMessageType());
    }

    public InetAddress getInetAddr() {
        return inetAddr;
    }

    public void setInetAddr(InetAddress inetAddr) {
        this.inetAddr = inetAddr;
    }
    public String messageString(){
        return(req.getMessage());
    }
    public int getPriority(){
        int priority = 0;
        switch (getMessageType()){
            case UPLOAD:
                return 4;
            case DOWNLOAD:
                return 3;
            case DELETE:
                return 1;
            default:
                return 0;
        }
    }

}
