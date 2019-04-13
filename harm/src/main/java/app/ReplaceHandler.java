package app;

import app.health_utils.IndexFile;
import app.health_utils.Indexer;

import java.io.File;
import java.net.Socket;

/**
 *
 */
public class ReplaceHandler implements Runnable {

    private final Request request;
    private String storage_path;
    private CommsHandler commLink;

    public ReplaceHandler(TcpPacket packet) {
        this.request = NetworkUtils.getPacketContents(packet);
        this.storage_path = "storage/";
        commLink = new CommsHandler();
    }

    @Override
    public void run() {





    }

}
