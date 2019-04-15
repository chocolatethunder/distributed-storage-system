package app;

import app.Debugger;
import app.ShutDown;

public class ShutdownHandler extends Thread {

    private ShutDown s;
    public ShutdownHandler(ShutDown s){
        super();
        this.s = s;
    }
    public void run(){
        Debugger.log("SHUTTING DOWN ALL THREADS", null);
        NetworkUtils.toggleShutdown(true);
        s.stop();
        Debugger.log("Please wait...", null);
        NetworkUtils.wait(5000);
        Debugger.log("Shutdown complete", null);

    }
}
