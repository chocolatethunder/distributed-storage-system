package app;

import app.LeaderUtils.CRUDQueue;
import app.LeaderUtils.QueueEntry;
import app.LeaderUtils.QueueHandler;
import app.chunk_utils.IndexFile;
import app.chunk_utils.Indexer;

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

    private int serverPort;
    private boolean running = true;
    private CRUDQueue pQueue;
    private static volatile IndexFile indexFile;
    public StalkerRequestHandler(CRUDQueue q, IndexFile index){
        this.pQueue = q;
        this.indexFile = index;
    }
    @Override
    public void run(){
        serverPort = ConfigManager.getCurrent().getLeader_report();
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();
        Socket client;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try{
            server = new ServerSocket(serverPort);
        }
        catch (IOException e){
            Debugger.log("", e);
            return;
        }
        Debugger.log("Stalker Request Handler: Leader is now taking requests on port ..." + serverPort, null);
        while(running){
            try{
                client = server.accept();
                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);
                Debugger.log("Stalker Request Handler: Accepted connection : " + client + ", Request type: " + req.getMessageType().name() + ".", null);

                //Acknowlegde that the request has been recieved and that
                MessageType m = req.getMessageType();
                if (m == MessageType.CONFIRM){
                    //allow a Stalker to start working
                    indexFile = Indexer.loadFromFile();
                    commLink.sendPacket(client, MessageType.CONFIRM, "", false);
                    commLink.sendPacket(client, MessageType.UPDATE, NetworkUtils.serializeObject(indexFile), false);
                }
                else if (m == MessageType.DISCOVER){
                    indexFile = Indexer.loadFromFile();
                    commLink.sendPacket(client, MessageType.ACK, NetworkUtils.serializeObject(indexFile), false);
                }
                else if (m == MessageType.UPLOAD || m == MessageType.DOWNLOAD || m == MessageType.DELETE){
                    //the socket can be closed client side
                    commLink.sendPacket(client, MessageType.ACK, "stalker request to queue", true);
                    //make a queueEntry with the request and Inet addr for later connection
                    QueueEntry toPut = new QueueEntry(req, client.getInetAddress());
                    executorService.submit(new QueueHandler(0, toPut, pQueue));
                }
            }
            catch (IOException e){
                Debugger.log("", e);
            }
        }

    }

}
