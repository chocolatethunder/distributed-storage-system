/*
 * Used to hold file metadata as well as a chunklist
 */
package app;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class Tester {
    //temp function for testing the fileDistributor
    public Tester(){}
    public List<String> harmList() {

        List<String> h = new ArrayList();
        h.add("harm_targets/00/");
        h.add("harm_targets/01/");
        h.add("harm_targets/02/");
        h.add("harm_targets/03/");
        h.add("harm_targets/04/");
        h.add("harm_targets/05/");
        h.add("harm_targets/06/");
        h.add("harm_targets/07/");
        h.add("harm_targets/08/");
        return(h);
    }

    public void test() {
        String input_file = "temp/temp/000_mp4_test.mp4";
        String chunk_dir = "temp/chunked/";
        String ass_dir = "temp/reassembled/";
        List<String> harm_list = harmList();
        FileChunker f = new FileChunker(chunk_dir);
        ChunkDistributor cd = new ChunkDistributor(chunk_dir, harm_list);
        ChunkRetriever cr = new ChunkRetriever(chunk_dir);
        ChunkAssembler ca = new ChunkAssembler(chunk_dir, ass_dir);
        //clean chunks first
        cleanChunks(harm_list, chunk_dir);
        //chunk file
        IndexEntry entry = f.chunkFile(input_file, 3);
        entry.summary();
        //distribute file
        if(cd.distributeChunks(entry, 3)){
            entry.cleanLocalChunks();
        }
        //retrieve chunks
        cr.retrieveChunks(entry);
        //assemble the chunks
        if(ca.assembleChunks(entry)){
            System.out.println("Test passed without fail");
        }


        entry.summary();
    }

    public void cleanChunks(List<String> h_list, String chunk_dir) {
        File folder = new File(chunk_dir);
        File[] flist = folder.listFiles();
        if (flist.length != 0) {
            for (int i = 0; i < flist.length; i++) {
                flist[i].delete();
            }

            for(String s : h_list){
                folder = new File(s);
                flist = folder.listFiles();
                for (int i = 0; i < flist.length; i++) {
                    flist[i].delete();
                }
            }
        }

    }

}
