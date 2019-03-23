package app.LeaderUtils;

import java.util.PriorityQueue;

/**
 *This is the ServiceHandler Factory that will create a a thread safe queue handler
 */
public class RequestAdministrator {

    private PriorityQueue<QueueEntry> pQueue;
    public RequestAdministrator(PriorityQueue<QueueEntry> q){
        this.pQueue = q;
    }
    

}
