package app;

import java.util.Set;

/**
 *
 */
public class ReplaceHandler implements Runnable {

   // private final Request request;
    private final String chunkId;
    private final Set<String> harmIps;
    private String storage_path;
    private CommsHandler commLink;

    public ReplaceHandler(String chunkId, Set<String> harmIps) {
        this.chunkId = chunkId;
        this.harmIps = harmIps;
        this.storage_path = "storage/";
        commLink = new CommsHandler();


    }

    @Override
    public void run() {





    }

}
