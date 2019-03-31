package app;

import app.handlers.ServiceHandlerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class ListenerThread implements Runnable{

    private final int serverPort = 11114;
    private boolean running = true;



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
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for health check or Leader Election request..");
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
                    executorService.submit(new HealthCheckReply(client));
                }

                //@Masroor add the Leader election logic here
                else{
                    running = false;
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
