package app;

import java.net.Socket;

/**
 *
 */
public class HealthCheckReply implements Runnable {

    private final Socket client ;

    public HealthCheckReply(Socket socket){
        client = socket;
    }
    @Override
    public void run() {

        CommsHandler commsHandler = new CommsHandler();
        commsHandler.sendPacket(this.client, MessageType.HEALTH_CHECK, "success");
    }
}
