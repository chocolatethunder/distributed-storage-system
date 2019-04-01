package app;

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



    public ListenerThread(){}


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
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for health check or Leader Election requests..");
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();
                System.out.println(NetworkUtils.timeStamp(1) + "Accepted connection : " + client);

                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);

                //checking for request type if health check
                if (req.getMessageType() == MessageType.HEALTH_CHECK){
                    System.out.println("Received health Check request");
                    executorService.submit(new HealthCheckResponder(client,
                            "SUCCESS",
                            getTotalSpaceFromHarms(),
                            Module.STALKER));
                }

                //@Masroor add the Leader election logic here
                else{
                    // reply to election local leader
                    executorService.execute(new ElectionResponder(client,req.getMessage()));
                    // reply to election stuff.


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
