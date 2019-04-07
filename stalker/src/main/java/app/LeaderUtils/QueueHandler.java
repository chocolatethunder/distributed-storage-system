package app.LeaderUtils;

import app.CommsHandler;
import app.MessageType;
import app.NetworkUtils;
import app.TcpPacket;
import app.chunk_utils.IndexFile;
import app.chunk_utils.IndexUpdate;
import app.chunk_utils.Indexer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class QueueHandler implements  Runnable {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    private int mode;
    private QueueEntry q;
    private CRUDQueue pQueue;
    public QueueHandler(int mode, QueueEntry q, CRUDQueue pQueue){
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
                    queueJob();
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
            //get ack that job is done
            if (commLink.sendPacket(worker, MessageType.START, "", true) == MessageType.DONE){
                //send permission to worker to update index

                //we may need to update the other stalkers
                if (q.getMessageType() == MessageType.UPLOAD || q.getMessageType() == MessageType.DELETE){
                    TcpPacket t = null;
                    t = commLink.receivePacket(worker);
                    sendUpdates(t);
                    System.out.println("Stalkers have been updated");

                }
                commLink.sendResponse(worker, MessageType.ACK);
                System.out.println(NetworkUtils.timeStamp(1) + " job complete");
                worker.close();
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
        System.out.println(NetworkUtils.timeStamp(1) + "Queuing job.");
        pQueue.add(q);
        System.out.println(NetworkUtils.timeStamp(1) + "Job Queued");

    }
    //remove entry from queue
    public void getJob(){
        q = pQueue.remove();
    }

    //send index update to all stalkers
    public boolean sendUpdates(TcpPacket t){
        CommsHandler commLink = new CommsHandler();
        int port = 11114;
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/stalkers.list"));
        List<Integer> s_list = NetworkUtils.mapToSList(m);
        Socket stalker = null;
        for (Integer id : s_list){
            String stalkerip =  m.get(id);
            try{
                stalker = NetworkUtils.createConnection(stalkerip, port);
                if (commLink.sendPacket(stalker,MessageType.UPDATE, t.getMessage(), true) == MessageType.ACK){
                    stalker.close();
                }

            }
            catch (IOException e){
                e.printStackTrace();
                return(false);
            }

            if (stalker != null){
                break;
            }
        }
        return(true);
    }

}
