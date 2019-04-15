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
        s.stop();
        Debugger.log("THREADS SHUTTING DOWN...", null);
        NetworkUtils.toggleShutdown(true);

    }
}
