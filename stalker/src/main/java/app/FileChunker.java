/*
 * The file chunker takes the path to a file and returns an
 * Unprocessed IndexEntry Object
 */
package app;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class FileChunker {
    private boolean debug = false;
    private String chunkDir;
    //hardcoded test variables

    public FileChunker(String c_dir) {
        chunkDir = c_dir;
    }
    //chunk a file into ideally a number determined by some predetermined policy
    //returns a list of the chunk paths
    public IndexEntry chunkFile(String filepath, int chunkCount){
        File file = new File(filepath);
        if (!file.exists()){
            return(null);
        }
        String file_prefix =  FilenameUtils.getBaseName(filepath);
        String file_type = FilenameUtils.getExtension(filepath);
        //init index entry
        IndexEntry iEnt = new IndexEntry(file_prefix, file_type, file.length());
        //set filesize
        //iEnt.setSize(file.length());
        if (debug) System.out.println("bytes: " + iEnt.size());

        List<Chunk> chunkRecord = new ArrayList();
        //the chunker should maybe check some sort of chunk policy policy
        //Policy p = checkPolicy()
        //Policy may have: chunk division number, maximum chunk size, ???

        //split into n chunks
        long chunkSize = iEnt.size()/chunkCount;
        if(debug) System.out.println("Chunksize: " + chunkSize);
        long bytesLeft = iEnt.size();
        for (int i = 0; i < chunkCount - 1; i++){
            if(debug) System.out.println(i + " bytes left: " + bytesLeft);
            Chunk record = newChunk(file, chunkSize * i, chunkSize , i);
            if (record == null) {return null;}
            bytesLeft = bytesLeft - chunkSize;
            chunkRecord.add(record);
        }
        Chunk record = newChunk(file, chunkSize * (chunkCount - 1), chunkSize, chunkCount -1);
        chunkRecord.add(record);
        //make sure nothing went wrong during the chunking
        for (Chunk c : chunkRecord) { if (c == null) return null;}
        iEnt.setChunks(chunkRecord);
        //assign chunks to the index entry
        return(iEnt);
    }

    //create a new chunk from a starting offset, reading num_bytes bytes
    public Chunk newChunk(File f, long start, long num_bytes, int cInd) {

        byte[] bytes = new byte[(int)num_bytes];
        Chunk tChunk;
        InputStream in;
        try{
            in = new FileInputStream(f);
            long offset = 0;
            in.skip(start);
            //read the bytes from the file
            while (offset < num_bytes) {
                int tmp = in.read(bytes, 0, (int)num_bytes);
                offset += tmp;
            }
            if (debug) System.out.println("offset: " + offset);
            in.close();
            //write bytes to new file
            //create the chunk object, the name
            tChunk = new Chunk(chunkDir, cInd, offset);
            FileUtils.writeByteArrayToFile(new File(tChunk.path()), bytes);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            System.out.println("ASS");
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("FUCK");
            return(null);
        }
        return(tChunk);
    }

    //--------------------------------- Utilities
    //prints contents of a text file
    public void printFileContents(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    //get the working directory
    public void printWorkingDir(){
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
    }


    public void debug() { debug = !debug; }


}
