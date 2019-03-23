package app;

import app.LeaderUtils.QueueEntry;
import app.LeaderUtils.RequestCoordinator;
import app.handlers.ServiceHandlerFactory;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/// the leader does not directly handle any work
// the leader is responsible for maintaining a total order on operations in the system
// the leader does this by maintaining a queue of requests sent by the stalkers
public class StalkerRequestHandler implements Runnable {

    private int serverPort = 11112;
    private boolean running = true;
    private PriorityQueue<QueueEntry> pQueue;
    public StalkerRequestHandler(PriorityQueue<QueueEntry> q){

    }
    @Override
    public void run(){
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();
        Socket client;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try{
            server = new ServerSocket(serverPort);
        }
        catch (IOException e){

            e.printStackTrace();
            return;
        }
        while(running){
            try{
                client = server.accept();
                System.out.println("Accepted connection : " + client);
                // receive packet on the socket link
                TcpPacket req = commLink.recievePacket(client);
                QueueEntry toPut = new QueueEntry(req, client);
                executorService.submit(RequestCoordinator.RequestManagerFactory(toPut));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

}
