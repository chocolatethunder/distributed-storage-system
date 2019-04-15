package app;

import app.chunk_util.IndexFile;
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
public class HealthListener implements Runnable{


    private int serverPort;
    private boolean running = true;
    private boolean verbose = false;
    private volatile IndexFile index;

    public HealthListener(){}
    public HealthListener(IndexFile ind){index = ind;}

    @Override
    public void run() {

        serverPort = ConfigManager.getCurrent().getHealth_check_port();

        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();

        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        try {
            server = new ServerSocket(serverPort);
            server.setSoTimeout(5000);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Debugger.log("Health Listener: Waiting for health check on port" + serverPort +  "...", null);
        // will keep on listening for requests
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown()) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();

                if (client != null) {
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
                }


            } catch (IOException e) {
                //Debugger.log("Listener: Socket timeout", null);
            }
        }
        Debugger.log("Health listener shutdown!", null);
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
