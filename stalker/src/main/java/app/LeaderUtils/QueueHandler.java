package app.LeaderUtils;

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

                break;
        }
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
