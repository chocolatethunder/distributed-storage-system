package app;

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

        if (Thread.interrupted()){
            exit = true;
        }


        while(!exit){
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

    }



    //will search for harm targets and stalkers on the network
    public void STALKERDiscovery(){
        Thread stalkerFinder;
        Thread harmFinder;
        Thread JCPlistener;
        Thread STALKERlistener;
        try {
            //listen for incoming requests first and foremost
            JCPlistener = new Thread(new DiscoveryReply(Module.STALKER, Module.JCP,timeout, verbose));
            STALKERlistener = new Thread(new DiscoveryReply(Module.STALKER, Module.STALKER,timeout, verbose));
            JCPlistener.start();
            STALKERlistener.start();
            //time out for a bit before sending out your own requests
            try {
                if (verbose){System.out.println("Waiting before sending out broadcast...");}
                Thread.sleep(5000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            //broadcast to harms and stalkers
            stalkerFinder = new Thread(new NetDiscovery(Module.STALKER, Module.STALKER,timeout, verbose));
            harmFinder = new Thread(new NetDiscovery(Module.HARM, Module.STALKER,timeout, verbose));
            harmFinder.start();
            stalkerFinder.start();
            //wait for threads to finish
            stalkerFinder.join();
            JCPlistener.join();
            STALKERlistener.join();
            harmFinder.join();
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
            broadcaster.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
