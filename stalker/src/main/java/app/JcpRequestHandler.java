package app;

import app.chunk_util.IndexFile;
import app.handlers.ServiceHandlerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *This is main thread that receives request through TCP from JCP
 */
public class JcpRequestHandler implements Runnable {

     private int serverPort;
     private boolean running = true;
     private IndexFile index;
     public JcpRequestHandler(IndexFile ind){
         this.index = ind;
     }

    /**
     *This is the main start method for this thread. It start in a while loop to receive connections from
     * JCP/s  and then executes handshake. Then it spawns a thread to handle the request.
     */
    @Override
    public void run() {
        serverPort =  ConfigManager.getCurrent().getJcp_req_port();
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();
        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            server = new ServerSocket(serverPort);
            server.setSoTimeout(1000);
            server.setReuseAddress(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Debugger.log("JCP request handler: Listening on port " + serverPort, null);
        Debugger.log("JCP server addr: " + server, null);
        // will keep on listening for requests
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown()) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();
                Debugger.log("JCP request handler: Accepted connection from JCP client:" + client, null);
                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);
                //creating a specific type of service handler using factory method
                //Submit a task to the handler queue and move on

                if(req.getMessageType() == MessageType.LIST){
                    executorService.submit(ServiceHandlerFactory.getServiceHandler(req, client, index));
                }

                if (NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path()).size() > 1 && req.getMessageType() != MessageType.LIST){
                    if (req.getMessageType() != MessageType.KILL){
                        executorService.submit(ServiceHandlerFactory.getServiceHandler(req, client, index));
                    }
                    else{
                        running = false;
                        client.close();
                    }
                }
                else{
                    if (req.getMessageType() != MessageType.LIST){
                        commLink.sendPacketWithoutAck(client, MessageType.BUSY, "Not enough Harms detected on network");
                    }
                }


            }
            catch (SocketTimeoutException ex){
            }
            catch (Exception e) {
            }
        }
        try{
            server.close();
            Debugger.log("Server closed", null);
        }
        catch (Exception e){
            Debugger.log("Error closing server", null);
        }
        Debugger.log("JCP Req: Service interrupted", null);
        executorService.shutdownNow();
    }


}

