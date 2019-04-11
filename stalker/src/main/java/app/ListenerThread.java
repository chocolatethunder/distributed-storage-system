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


    private final int serverPort = ConfigManager.getCurrent().getElection_port();
    private boolean running = false;
    private boolean verbose = true;
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
            server.setSoTimeout(5000);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Debugger.log("Health Listener: Waiting for health check, Leader Election requests, or updates...", null);
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();

                if (verbose){Debugger.log("Health Listener: Accepted connection : "  + client, null);}
                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);

                //checking for request type if health check
                if (req.getMessageType() == MessageType.HEALTH_CHECK){
                    if (verbose){
                        Debugger.log("Health Listener: Received health Check request : ", null);}
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
                else if (req.getMessageType() == MessageType.REELECT){
                    Debugger.log("Re-election requested...", null);
                    ConfigManager.getCurrent().setReelection(true);
                    //executorService.submit(new LeaderResponder(client));
                }
                else if(req.getMessageType() == MessageType.UPDATE){
                    // Update the indexfile
                    Debugger.log("Update received from leader", null);
                    //commLink.sendResponse(client, MessageType.ACK);
                    executorService.execute(new IndexManager(index, Indexer.deserializeUpdate(req.getMessage())));
                }
            } catch (IOException e) {
                //Debugger.log("Listener: Socket timeout", null);
            }
        }

    }


    /**
     * Adds the total space from harm list
     * @return
     */
    public long getTotalSpaceFromHarms(){
        long total = 0;

        Map<Integer, String> harms = NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getHarm_list_path()));
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<Integer, String> entry : harms.entrySet()) {
            NodeAttribute attributes = null;
            try {
                attributes = mapper.readValue(entry.getValue(), NodeAttribute.class);
                total += attributes.getSpace();
            } catch (IOException e) {
                Debugger.log("", e);
            }

        }
        return total;
    }
}
