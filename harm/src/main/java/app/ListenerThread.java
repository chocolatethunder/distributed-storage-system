package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import app.ConfigManager;

/**
 *This listener thread is dedicated to listening for HEALTH check requests
 */
public class ListenerThread implements Runnable {


    private ConfigFile cfg;
    private int serverPort;
    private boolean running = true;
    private boolean debugMode = true;


    public ListenerThread(boolean debugMode){
        this.debugMode = debugMode;
    }

    @Override
    public void run() {
        serverPort = ConfigManager.getCurrent().getHealth_check_port();
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();

        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            server = new ServerSocket(serverPort);

        } catch (IOException e) {
            Debugger.log("", e);
        }
        Debugger.log("Listener: Harm server: Waiting for health check requests from stalkers..", null);
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a STALKER
                Socket client = server.accept();
                if(debugMode) {
                   // Debugger.log("DiscManager: Harm server: Accepted connection from stalker : " + client, null);

                }

                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);

                //checking for request type if health check
                if (req.getMessageType() == MessageType.HEALTH_CHECK) {
                    if(debugMode) {
                        //Debugger.log("DiscManager: Harm server: Received health Check request", null);
                    }

                    //_______TO:DO check for corrupted chunks here

                    executorService.submit(new HealthCheckResponder(client,
                            "SUCCESS",
                            getAvailableDiskSpace(),
                            null,
                            Module.HARM));
                }
                else {
                    running = false;
                    //client.close();
                }
            } catch (IOException e) {
                Debugger.log("", e);
            }
        }

    }

    /**
     * This method returns total space available in root directory and all subdirectories
     * @return
     */
    public long getAvailableDiskSpace() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        long total = 1000000;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            Debugger.log(root + ": ", null);
            try {
                FileStore store = Files.getFileStore(root);
                total += store.getUsableSpace();
                if(debugMode) {
                    Debugger.log("DiscManager: available=" + nf.format(store.getUsableSpace())
                            + ", total=" + nf.format(store.getTotalSpace()), null);
                }
            } catch (IOException e) {
                if(debugMode) {
                    Debugger.log("DiscManager: Harm server: error querying space:  + e.toString()", e);
                }
            }
        }

        return total;
    }

}
