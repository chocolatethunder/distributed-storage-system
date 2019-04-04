package app;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryManager implements Runnable{

    private Module mType;
    private int timeout = 20;
    private boolean verbose = false;
    private boolean exit = false;
    public DiscoveryManager(Module m){
        mType = m;
    }
    public DiscoveryManager(Module m, int timeout){
        mType = m;
        this.timeout = timeout;
    }
    public DiscoveryManager(Module m, int timeout, boolean verbose){
        mType = m;
        this.timeout = timeout;
        this.verbose = verbose;
    }
    @Override
    public void run(){
        //run forever
    while(!Thread.interrupted()){
        switch (mType){
            case STALKER:
                STALKERDiscovery();
                break;
            case HARM:
                HARMDiscovery();
                break;
            case JCP:
                JCPDiscovery();
                break;
            default:
                return;
        }
    }
    System.out.println("interrupted");

    }


    public void close(){
        exit = true;
    }

    //will search for harm targets and stalkers on the network
    public void STALKERDiscovery(){

        List<Thread> threads = new ArrayList<>();
        try {
            //listen for incoming requests first and foremost

            //jcp listener
            threads.add(new Thread(new DiscoveryReply(Module.STALKER, Module.JCP,timeout, verbose)));
            //stalker listener
            threads.add(new Thread(new DiscoveryReply(Module.STALKER, Module.STALKER,timeout, verbose)));
            threads.get(0).start();
            threads.get(1).start();
            //time out for a bit before sending out your own requests
            try {
                if (verbose){System.out.println(NetworkUtils.timeStamp(1) + "Waiting before sending out broadcast...");}
                Thread.sleep(5000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            //broadcast to harms and stalkers
            threads.add(new Thread(new NetDiscovery(Module.STALKER, Module.STALKER,timeout, verbose)));
            threads.add(new Thread(new NetDiscovery(Module.HARM, Module.STALKER,timeout, verbose)));
            threads.get(2).start();
            threads.get(3).start();

            //The threads will never stop so we must stop them when this thread is interrupted
            while (!Thread.interrupted()){
                try{Thread.sleep(10000);}catch (Exception e){};
            }
            threads.forEach(t -> t.interrupt());
            for (Thread t : threads){
                try{
                    t.join();
                }
                catch (InterruptedException e){
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //will listen for udp packets from stalkers
    public void HARMDiscovery(){
        Thread listener;
        try{
            //listen for incoming requests first and foremost
            listener = new Thread(new DiscoveryReply(Module.HARM, Module.STALKER, timeout, verbose));
            listener.start();

            while(!checkInterrupted());
            listener.interrupt();
            listener.join();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //will broadcast on network to find stalkers
    public void JCPDiscovery(){
        Thread broadcaster;
        try {
            broadcaster = new Thread(new NetDiscovery(Module.STALKER, Module.JCP,timeout, verbose));
            broadcaster.start();
            while(!checkInterrupted());
            broadcaster.interrupt();
            broadcaster.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkInterrupted(){
        try{Thread.sleep(10000);}catch (Exception e){};
        if (Thread.interrupted()){
            return true;
        }
        else{
            return false;
        }

    }


}
