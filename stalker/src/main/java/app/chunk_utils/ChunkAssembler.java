/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app.chunk_utils;
import app.Debugger;
import app.chunk_util.Chunk;
import app.chunk_util.IndexEntry;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;

public class ChunkAssembler {
    private boolean debug = false;
    private String chunkDir;
    //where the file will be assembled
    private String ass_dir;
    //hardcoded test variables

    public ChunkAssembler(String c_dir, String a_dir) {
        chunkDir = c_dir;
        ass_dir = a_dir;
    }

    public boolean assembleChunks(IndexEntry iEnt){
        for (Chunk c : iEnt.getChunkList()){
            if (c.getChunk_path() == ""){return false;}
            if(!writeChunkTofile(c, iEnt.fileName())){
                Debugger.log("ChunkAssembler: assembly failed!", null);
                return false;
            }
        }
        return true;
    }

    //write to the assembled directory
    public boolean writeChunkTofile(Chunk c, String fileName){
        try{
            File f = new File(ass_dir + fileName);
            OutputStream os = new FileOutputStream(f, true);
            byte[] chunk_data = Files.readAllBytes(Paths.get(c.getChunk_path()));
            os.write(chunk_data, 0, (int)c.getChunk_size());
            os.close();
            return true;
        }
        catch (IOException e){
            Debugger.log("", e);
            return false;
        }
        catch (NullPointerException e){
            Debugger.log("ChunkAssembler: Cannot find assembled dir!", e);
            return false;
        }

    }

    public void debug() { debug = !debug; }


}
