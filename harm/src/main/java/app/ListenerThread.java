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

/**
 *This listener thread is dedicated to listening for HEALTH check requests
 */
public class ListenerThread implements Runnable {

    private final int serverPort;
    private boolean running = true;


    public ListenerThread(int port) {
        serverPort = port;
    }

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
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for health check requests from stalkers..");
        // will keep on listening for requests
        while (running) {
            try {
                //accept connection from a JCP
                Socket client = server.accept();
                System.out.println(NetworkUtils.timeStamp(1) + "Accepted connection : " + client);

                // receive packet on the socket link
                TcpPacket req = commLink.receivePacket(client);

                //checking for request type if health check
                if (req.getMessageType() == MessageType.HEALTH_CHECK) {
                    System.out.println("Received health Check request");

                    //_______TO:DO check for corrupted chunks here

                    executorService.submit(new HealthCheckResponder(client,
                            "SUCCESS",
                            getAvailableDiskSpace(),
                            null,
                            Module.HARM));
                }
                else {
                    running = false;
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method returns total space available in root directory and all subdirectories
     * @return
     */
    public long getAvailableDiskSpace() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        long total = 0;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {

            System.out.print(root + ": ");
            try {
                FileStore store = Files.getFileStore(root);
                total += store.getUsableSpace();
                System.out.println("available=" + nf.format(store.getUsableSpace())
                        + ", total=" + nf.format(store.getTotalSpace()));
            } catch (IOException e) {
                System.out.println("error querying space: " + e.toString());
            }
        }

        return total;
    }

}
