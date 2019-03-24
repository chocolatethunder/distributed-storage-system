package app;

import app.LeaderUtils.QueueEntry;
import app.LeaderUtils.QueueHandler;

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
        System.out.println(NetworkUtils.timeStamp(1) + "Leader is now taking requests...");
        while(running){
            try{
                client = server.accept();
                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);
                System.out.println(NetworkUtils.timeStamp(1) + "Accepted connection : " + client + ", Request type: " + req.getMessageType().name() + ".");
                //Acknowlegde that the request has been recieved and that
                //the socket can be closed client side
                commLink.sendPacket(client, MessageType.ACK, "");
                //make a queueEntry with the request and Inet addr for later connection
                QueueEntry toPut = new QueueEntry(req, client.getInetAddress());
                executorService.submit(new QueueHandler(0, toPut, pQueue));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

}
