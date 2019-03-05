/*
 * This Class will be responsible for replicating and distributing the chunks
 * among the harm targets
 */
package app;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class ChunkDistributer {
    private boolean debug = false;
    private String chunkDir;

    //for now this will be a set of directories of "harm targets"
    //in the future these will be some sort of address
    List<String> harm_list;
    //hardcoded test variables
    public ChunkDistributer(String c_dir, List<String> h_list) {
        chunkDir = c_dir;
        harm_list = h_list;

    }

    //take an index entry object and process distribute
    //will use round robin for now
    public boolean distributeChunks(IndexEntry iEnt, int num_reps) {

    }

    public void debug() { debug = !debug; }

}
