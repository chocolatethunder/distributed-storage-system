package app.LeaderUtils;

import app.CommsHandler;
import app.MessageType;
import app.NetworkUtils;
import app.TcpPacket;

import java.io.IOException;
import java.net.Socket;
import java.util.PriorityQueue;

public class QueueHandler implements  Runnable {

    private int mode;
    private QueueEntry q;
    private PriorityQueue<QueueEntry> pQueue;
    public QueueHandler(int mode, QueueEntry q, PriorityQueue<QueueEntry> pQueue){
        this.pQueue = pQueue;
        this.mode = mode;
        this.q = q;
    }

    @Override
    public void run(){

        switch(mode){
            case 0:
                queueJob();
                break;
            case 1:
                getJob();
                if (!processJob()){
                    //if it fails we'll put it back in the queue
                    //queueJob();
                }
                break;
        }
    }


    public boolean processJob(){
        CommsHandler commLink = new CommsHandler();
        Socket worker;
        try{
            worker = NetworkUtils.createConnection(q.getInetAddr().getHostAddress(), 11113);
            //connect and grant permission to edit file
            commLink.sendPacket(worker, MessageType.START, "");
            //get ack that job is done
            TcpPacket response = commLink.receivePacket(worker);
            if (response.getMessageType() == MessageType.DONE){
                //send permission to worker
                commLink.sendPacket(worker, MessageType.ACK, "");
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return(false);
        }


        return true;
    }
    //put an entry into the queue
    public synchronized void queueJob(){
        pQueue.add(q);
    }
    //remove entry from queue
    public synchronized void getJob(){
        q = pQueue.remove();
    }

}
