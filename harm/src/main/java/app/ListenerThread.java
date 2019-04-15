package app;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.health_utils.HealthStat;

/**
 *This listener thread is dedicated to listening for HEALTH check requests
 */
public class ListenerThread implements Runnable {


    private ConfigFile cfg;
    private int serverPort;
    private boolean running = true;
    private boolean debugMode = true;
    private boolean halt = false;

    public ListenerThread(boolean debugMode){
        this.debugMode = debugMode;
    }

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
            Debugger.log("", e);
        }
        Debugger.log("Listener: Harm server: Waiting for health check requests from stalkers..", null);
        // will keep on listening for requests
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown()) {
            try {
                //accept connection from a STALKER
                Socket client = server.accept();
                if(debugMode) {
                   // Debugger.log("DiscManager: Harm server: Accepted connection from stalker : " + client, null);

                }
                if (client !=  null){
                    // receive packet on the socket link
                    TcpPacket req = commLink.receivePacket(client);

                    //checking for request type if health check
                    if (req.getMessageType() == MessageType.HEALTH_CHECK) {
                        if(debugMode) {
                            //Debugger.log("DiscManager: Harm server: Received health Check request", null);
                        }
                        //check for corrupted chunks here
                        // I fixed it with my ingeniousness
                        Map<String, String> corruptList = HealthStat.getInstance().getCorruptList();
                        if(corruptList.isEmpty()) {

                            executorService.submit(new HealthCheckResponder(client,
                                    "SUCCESS",
                                    getAvailableDiskSpace(),
                                    new HashSet<>(), // sending empty set
                                    Module.HARM));
                        }else{
                            executorService.submit(new HealthCheckResponder(client,
                                    "CORRUPT",
                                    getAvailableDiskSpace(),
                                    corruptList.keySet(),
                                    Module.HARM));

                        }
                }

                }
                else {
                    running = false;
                    //client.close();
                }

            }
            catch (SocketTimeoutException e){

            }
            catch (IOException e) {
                Debugger.log("", e);
            }

        }
        Debugger.log("Harm Listener shutdown safely!", null);

    }
    public void stop(){halt = !halt;}

    /**
     * This method returns total space available in root directory and all subdirectories
     * @return
     */
    public long getAvailableDiskSpace() {
//        NumberFormat nf = NumberFormat.getNumberInstance();
//        long total = 1000000;
//        for (Path root : FileSystems.getDefault().getRootDirectories()) {
//            Debugger.log(root + ": ", null);
//            try {
//                FileStore store = Files.getFileStore(root);
//                total += store.getUsableSpace();
//                if(debugMode) {
//                    Debugger.log("DiscManager: available=" + nf.format(store.getUsableSpace())
//                            + ", total=" + nf.format(store.getTotalSpace()), null);
//                }
//            } catch (IOException e) {
//                if(debugMode) {
//                    Debugger.log("DiscManager: Harm server: error querying space:  + e.toString()", e);
//                }
//            }
//        }

        long total = new File("/").getUsableSpace();

        return total;
    }

}
