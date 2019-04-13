package app.LeaderUtils;

import app.*;
import app.chunk_utils.IndexFile;
import app.chunk_utils.IndexUpdate;
import app.chunk_utils.Indexer;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

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
                    //queueJob();
                }
                break;
        }
    }
    public boolean processJob(){
        CommsHandler commLink = new CommsHandler();
        Socket worker = null;
        try{
            System.out.println(NetworkUtils.timeStamp(1) + " Processing job...");
            worker = NetworkUtils.createConnection(q.getInetAddr().getHostAddress(), ConfigManager.getCurrent().getLeader_admin_port());
            //connect and grant permission to edit file
            //get ack that job is done
            if (commLink.sendPacket(worker, MessageType.START, "", true) == MessageType.DONE){
                //send permission to worker to update index

                //we may need to update the other stalkers
                if (q.getMessageType() == MessageType.UPLOAD || q.getMessageType() == MessageType.DELETE){
                    TcpPacket t = null;
                    t = commLink.receivePacket(worker);
                    sendUpdates(t);
                    Thread th = new Thread(new IndexManager(Indexer.loadFromFile(), Indexer.deserializeUpdate(t.getMessage())));
                    th.start();
                    th.join();
                    Debugger.log("Stalkers have been updated", null);

                }
                commLink.sendResponse(worker, MessageType.ACK);
                Debugger.log( "job complete", null);
                worker.close();
            }
        }
        catch (InterruptedException e){
            Debugger.log("", e);
        }
        catch (IOException e){

            e.printStackTrace();
            return(false);
        }
        catch (Exception e){
            Debugger.log("", e);
        }
        try{
            worker.close();
        }
        catch (Exception e){

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
        int port = ConfigManager.getCurrent().getElection_port();
        Debugger.log("Sending out updates...", null);
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getStalker_list_path()));
        m.remove(ConfigManager.getCurrent().getLeader_id());
        List<Integer> s_list = NetworkUtils.mapToSList(m);
        Socket stalker = null;
        for (Integer id : s_list){

            int attempts = 0;
            String stalkerip =  m.get(id);
            Debugger.log("Sending update to " + stalkerip, null);
            while(attempts < 1){
                try{
                    stalker = NetworkUtils.createConnection(stalkerip, port);

                    if(commLink.sendPacket(stalker,MessageType.UPDATE, t.getMessage(), true) == MessageType.ACK){
                        stalker.close();
                    }

                }
                catch (IOException e){
                    Debugger.log("",e);
                    e.printStackTrace();
                }
                catch (Exception e){
                    Debugger.log("",e);
                }
                attempts++;
            }

        }
        return(true);
    }

}
