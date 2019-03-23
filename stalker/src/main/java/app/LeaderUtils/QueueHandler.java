package app.LeaderUtils;

import java.util.PriorityQueue;

public class QueueHandler implements  Runnable {

    private int mode;
    private QueueEntry q;
    public QueueHandler(int mode, QueueEntry q){
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
                dispatchJob();
                break;
        }
    }
    public synchronized void queueJob(){

    }

    public synchronized void dispatchJob(){

    }
}
