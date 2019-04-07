package app;

import app.LeaderUtils.IndexManager;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the listener thread for STALKER, excepting health check requests from JCP and other stalkers
 */
public class ListenerThread implements Runnable{

    private final int serverPort = 11114;
    private boolean running = true;
    private boolean verbose = false;
    private volatile IndexFile index;

    public ListenerThread(){}
    public ListenerThread(IndexFile ind){index = ind;}

    @Override
    public void run() {

        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();

        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            server = new ServerSocket(serverPort);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for health check, Leader Election requests, or updates");
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();
                if (verbose){System.out.println(NetworkUtils.timeStamp(1) + "Accepted connection : " + client);}
                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);

                //checking for request type if health check
                if (req.getMessageType() == MessageType.HEALTH_CHECK){
                    if (verbose){System.out.println("Received health Check request");}
                    executorService.submit(new HealthCheckResponder(client,
                            "SUCCESS",
                            getTotalSpaceFromHarms(),
                            Module.STALKER));
                }

                //When a leader request is recieved
                else if(req.getMessageType() == MessageType.LEADER){
                    // reply to with leader
                    executorService.submit(new LeaderResponder(client));

                }
                else if(req.getMessageType() == MessageType.UPDATE){
                    // Update the indexfile
                    commLink.sendResponse(client, MessageType.ACK);
                    executorService.submit(new IndexManager(index, Indexer.deserializeUpdate(req.getMessage())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Adds the total space from harm list
     * @return
     */
    public long getTotalSpaceFromHarms(){
        long total = 0;

        Map<Integer, String> harms = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<Integer, String> entry : harms.entrySet()) {
            NodeAttribute attributes = null;
            try {
                attributes = mapper.readValue(entry.getValue(), NodeAttribute.class);
                total += attributes.getSpace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return total;
    }
}
