package app;

import java.util.List;

public class ShutDown {
     private List<Thread> threads;
     public ShutDown(List<Thread> l){
         threads = l;
         for(Thread t : threads){
             t.start();
         }
     }

     public void stop(){
         for(Thread t : threads){
             t.interrupt();
         }
     }



}
