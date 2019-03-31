package app;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 *This class is the Health Check worker thread that will send the Health Check Packet to the requester
 */
public class HealthCheckResponder implements Runnable {

    private final Socket socket;
    private final String status;
    private final long diskSpace;
    private  Map<Integer, Integer> corruptedChunks;

    /**
     * This constructor will be used by HARM when there is corrupted chunks detected
     * @param socket
     * @param status
     * @param diskSpace
     * @param corruptedChunks
     */
    public HealthCheckResponder(Socket socket, String status, long diskSpace, Map<Integer, Integer> corruptedChunks){
        this.socket = socket;
        this.status = status;
        this.diskSpace = diskSpace;
        this.corruptedChunks = corruptedChunks;

    }

    /**
     * Contructor for using with success packets for STALKERS
     * @param socket
     * @param status
     * @param diskSpace
     */
    public HealthCheckResponder(Socket socket, String status, long diskSpace){
        this.socket = socket;
        this.status = status;
        this.diskSpace = diskSpace;

    }
    @Override
    public void run(){

            System.out.println("Inside Health responder");
            CommsHandler commsHandler = new CommsHandler();
            commsHandler.sendPacketWithoutAck(this.socket, MessageType.HEALTH_CHECK, NetworkUtils.createHealthCheckReply(status,
                    diskSpace,
                    corruptedChunks));

//            try{
//                System.out.println("Closing connection..");
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

    }
}