package app;

import app.chunk_utils.IndexFile;
import app.CommsHandler;
import app.handlers.ServiceHandlerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *This is main thread that receives request through TCP from JCP
 */
public class JcpRequestHandler implements Runnable {

     private final int serverPort = 11111;
     private boolean running = true;
     private IndexFile index;
     public JcpRequestHandler(IndexFile ind){
         this.index = ind;
     }

    /**
     *This is the main run method for this thread. It run in a while loop to receive connections from
     * JCP/s  and then executes handshake. Then it spawns a thread to handle the request.
     */
    @Override
    public void run() {

        //initialize socket and input stream
        //Socket socket = null;
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();

//        DataInputStream in = null;
//        DataOutputStream out = null;


        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            server = new ServerSocket(serverPort);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Waiting...");
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();
                System.out.println("Accepted connection : " + client);
                // receive packet on the socket link
                TcpPacket req = commLink.recievePacket(client);

                //creating a specific type of service handler using factory method
                //Submit a task to the handler queue and move on
                if (req.getMessageType() != MessageType.KILL){
                    executorService.submit(ServiceHandlerFactory.getServiceHandler(req, client, index));
                }
                else{
                    running = false;
                    client.close();
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

