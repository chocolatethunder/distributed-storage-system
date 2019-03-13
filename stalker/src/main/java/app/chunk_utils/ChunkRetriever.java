/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app.chunk_utils;
import java.io.*;

import org.apache.commons.io.FileUtils;

public class ChunkRetriever {
    private boolean debug = false;
    private String chunkDir;
    //hardcoded test variables

    public ChunkRetriever(String c_dir) {
        chunkDir = c_dir;
    }


    public boolean retrieveChunks(IndexEntry iEnt){
        // each chunk indexd in the entry try and get a chunk from one of the
        //nodes that carries it
        for (Chunk c : iEnt.getChunks()){
            if(!retrieveChunk(c)){
                return(false);
            }
        }
        return true;

    }

    //retreives a chunk from a node
    //if it cannot get any copies of the chunk it will fail and return false
    //in the future will retrieve it from a remote node
    public boolean retrieveChunk(Chunk c){
        for (String s : c.getReplicaAddrs()){
            try{
                FileUtils.copyFile(new File(s),new File(chunkDir + c.hash()));
                c.setPath(chunkDir + c.hash());
                return true;
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        return(false);
    }



    public void debug() { debug = !debug; }


}
