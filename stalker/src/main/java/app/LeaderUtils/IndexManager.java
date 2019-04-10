package app.LeaderUtils;
import app.TcpPacket;
import app.chunk_utils.IndexFile;
import app.chunk_utils.IndexUpdate;
import app.chunk_utils.Indexer;

public class IndexManager implements Runnable {

    private volatile IndexFile index;
    IndexUpdate update;

    public IndexManager(IndexFile ind, IndexUpdate u){
        index = Indexer.loadFromFile();
        update = u;
    }
    @Override
    public void run(){
        switch (update.getMt()){
            case UPLOAD:
                Indexer.addEntry(index,update.getEntry());

                break;
            case DELETE:
                Indexer.removeEntry(index,update.getEntry());
                break;
        }
        Indexer.saveToFile(index);
    }



}
