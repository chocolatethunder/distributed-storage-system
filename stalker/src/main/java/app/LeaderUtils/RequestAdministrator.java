package app.LeaderUtils;

import app.Debugger;
import app.NetworkUtils;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *This class will be responsible for taking requests out of the queue and granting permission to perform the action
 */
public class RequestAdministrator implements Runnable {

    private CRUDQueue pQueue;
    public RequestAdministrator(CRUDQueue q){
        this.pQueue = q;
    }

    @Override
    public void run(){
        System.out.println(NetworkUtils.timeStamp(1) + "Administrator online.");
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown()){

            if (!pQueue.isEmpty()){
                executorService.submit(new QueueHandler(1,null, pQueue));
            }
            try{
                //lets not make this too busy if nothing is going on
                Thread.sleep((long)(Math.random() * 1000));
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        Debugger.log("Request admin shutdown...", null);
    }

}
