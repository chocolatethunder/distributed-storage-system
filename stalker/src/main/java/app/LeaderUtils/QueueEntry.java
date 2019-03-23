package app.LeaderUtils;

import app.TcpPacket;
import java.net.Socket;
import app.MessageType;

public class QueueEntry {
    private TcpPacket req;
    Socket worker;
    public QueueEntry(TcpPacket t, Socket s){
        req = t;
        worker = s;
    }

    public Socket getWorker() {
        return worker;
    }

    public TcpPacket getReq() {
        return req;
    }
    public MessageType getMessageType(){
        return(req.getMessageType());
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
