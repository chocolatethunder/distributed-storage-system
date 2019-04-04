package app.LeaderUtils;

import java.util.Comparator;
import java.util.PriorityQueue;


//this is a specialized, synchronized priority queue based on CRUD priority
public class CRUDQueue {
    private PriorityQueue<QueueEntry> pQueue;
    public CRUDQueue(){

        Comparator<QueueEntry> entryPriorityComparator = new Comparator<QueueEntry>() {
            @Override
            public int compare(QueueEntry q1, QueueEntry q2) {
                return q1.getPriority() - q2.getPriority();
            }
        };
        this.pQueue = new PriorityQueue<>(entryPriorityComparator);
    }

    public synchronized void add(QueueEntry q){pQueue.add(q); }
    public synchronized QueueEntry remove(){ return(pQueue.remove()); }
    public synchronized boolean isEmpty(){return(pQueue.isEmpty());}

    public PriorityQueue<QueueEntry> getpQueue() {return pQueue;}
    public void setpQueue(PriorityQueue<QueueEntry> pQueue) {this.pQueue = pQueue;}
}
