package app.LeaderUtils;

import app.CommsHandler;
import app.MessageType;
import app.NetworkUtils;
import app.TcpPacket;

import java.io.IOException;
import java.net.Socket;
import java.util.PriorityQueue;

public class QueueHandler implements  Runnable {

    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

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
        System.out.println("inside function");
        switch(mode){
            case 0:
                System.out.println(NetworkUtils.timeStamp(1) + "Queuing job.");
                queueJob();
                System.out.println("Job Queued");
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
            System.out.println(NetworkUtils.timeStamp(1) + " Processing job...");
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
    public void queueJob(){
        synchronized(lock1){
            pQueue.add(q);
        }

    }
    //remove entry from queue
    public void getJob(){
        synchronized(lock1){
            q = pQueue.remove();
        }

    }

}
