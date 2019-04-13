package app;


import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ReplaceHandler implements Runnable {


    private final String chunkId;
    private final List<String> harmIps;
    private String storage_path_temp;
    private final String storage_path = "storage/";
    private CommsHandler commLink;

    public ReplaceHandler(String chunkId, Set<String> harmIps) {
        this.chunkId = chunkId;
        this.harmIps = new ArrayList<>();
        this.harmIps.addAll(harmIps);
        this.storage_path_temp = "storage/temp/";
        commLink = new CommsHandler();
    }

    @Override
    public void run() {

        //get the map of harm ids
        //HashMap<Integer, String > m = NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
        Map<Integer, NodeAttribute> n = NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path());
        int index = 0;

        Socket harmServer = null;

        while( (harmServer = connectionSuccess()) != null){
             // do nothing , we just want one successfull connection
        }


        try {
            if (commLink.sendPacket(harmServer, MessageType.DOWNLOAD,
                    NetworkUtils.createSerializedRequest(this.chunkId, MessageType.DOWNLOAD, ""), true) == MessageType.ACK) {
                FileStreamer fileStreamer = new FileStreamer(harmServer);
                fileStreamer.receiveFileFromSocket(this.storage_path_temp + this.chunkId);


                Path copiedChunk = Paths.get(this.storage_path_temp + this.chunkId);
                // delete the files

                if(copiedChunk != null) {
                    File f = new File(storage_path + this.chunkId);
                    f.delete();


                    Path move = Files.move(copiedChunk, Paths.get(storage_path + this.chunkId));

                    if(move != null){
                        // success
                    }else{
                        //failure
                    }

                }

                harmServer.close();
            }

        } catch (IOException e) {
            Debugger.log("Chunk Retriever: Attempt: " + " failed!", e);
        }
    }




    private Socket connectionSuccess(){


        try {
            Socket harmServer = NetworkUtils.createConnection(this.harmIps.get(0), ConfigManager.getCurrent().getHarm_listen());
            return  harmServer;
        } catch (IOException e) {
           return  null;
        }
    }

}
