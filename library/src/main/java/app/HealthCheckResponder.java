package app;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

/**
 *This class is the Health Check worker thread that will send the Health Check Packet to the requester
 */
public class HealthCheckResponder implements Runnable {

    private final Socket socket;
    private final String status;
    private final long diskSpace;
    private Set<String> corruptedChunks;
    private final Module sender;

    /**
     * This constructor will be used by HARM when there is corrupted chunks detected
     * @param socket
     * @param status
     * @param diskSpace
     * @param corruptedChunks
     */
    public HealthCheckResponder(Socket socket,
                                String status,
                                long diskSpace,
                                Set<String> corruptedChunks,
                                Module sender){
        this.socket = socket;
        this.status = status;
        this.diskSpace = diskSpace;
        this.corruptedChunks = corruptedChunks;
        this.sender = sender;

    }

    /**
     * Contructor for using with success packets for STALKERS
     * @param socket
     * @param status
     * @param diskSpace
     */
    public HealthCheckResponder(Socket socket,
                                String status,
                                long diskSpace,
                                Module sender){
        this.socket = socket;
        this.status = status;
        this.diskSpace = diskSpace;
        this.sender = sender;

    }
    @Override
    public void run(){

            CommsHandler commsHandler = new CommsHandler();
            commsHandler.sendPacketWithoutAck(this.socket, MessageType.HEALTH_CHECK,
                    NetworkUtils.createHealthCheckReply(this.sender,
                            status,
                    diskSpace,
                    corruptedChunks ));

            try{
                socket.close();
            } catch (IOException e) {
                Debugger.log("", e);
            }

    }
}
