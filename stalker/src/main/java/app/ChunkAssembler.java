/*
 * This Class will retrieve the chunks needed for the file assembler to
 * reassemble the file
 *
 */
package app;
import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

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
        for (Chunk c : iEnt.getChunks()){
            if (c.path() == ""){return false;}
            if(!writeChunkTofile(c, iEnt.fileName())){
                System.out.println("assembly failed!");
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
            byte[] chunk_data = Files.readAllBytes(Paths.get(c.path()));
            os.write(chunk_data, 0, (int)c.size());
            os.close();
            return true;
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
        catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Cannot find assembled dir!");
            return false;
        }

    }

    public void debug() { debug = !debug; }


}
