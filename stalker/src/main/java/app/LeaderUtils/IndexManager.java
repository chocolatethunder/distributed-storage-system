package app.LeaderUtils;
import app.chunk_util.IndexFile;
import app.chunk_util.IndexUpdate;
import app.chunk_util.Indexer;

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
