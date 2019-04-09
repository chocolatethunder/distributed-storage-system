package app.chunk_utils;

import app.Debugger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.Optional;
import app.NetworkUtils;

//This class will be in control of the indexFiles
//all these functions are probably going to need to be thread safe
public class Indexer {
    public static String indexPath;
    //loads the json from file and converts it to an IndexFile object
    public static synchronized IndexFile loadFromFile(){
        ObjectMapper mapper = new ObjectMapper();
        Optional<IndexFile> ind = Optional.empty();
        try {
            File f = new File(indexPath);
            if (!f.exists()){
                throw new FileNotFoundException("Indexer: Index file not found...");
            }
            ind = Optional.of(mapper.readValue(NetworkUtils.fileToString(indexPath), IndexFile.class));
            System.out.println("Indexer: Indexfile loaded from file.");
        }
        catch (IOException e){
            //if the file is corrupt or empty we create a new IndexFile
            Debugger.log("", e);
            System.out.println("Indexer: Creating new indexfile");
            IndexFile temp = new IndexFile();
            saveToFile(temp);
            return(temp);
        }
        return ind.get();
    }

    public static synchronized IndexFile fromString(String serial){
        ObjectMapper mapper = new ObjectMapper();
        Optional<IndexFile> ind = Optional.empty();
        try{
            ind = Optional.of(mapper.readValue(serial, IndexFile.class));
        }
        catch(Exception e){
           Debugger.log("", e);
           return null;
        }
        System.out.println("Indexer: Indexfile loaded from file.");
        return ind.get();
    }


    //Save index to file
    public static synchronized boolean saveToFile(IndexFile ind){
        String tempfile = "index//indexFile/main";
        try{
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(ind);

            File temp = new File(tempfile);
            File indexfile = new File(indexPath);
            if (temp.exists()){
                temp.delete();
            }
            //must be thread safe
            ///////////////////////////////////////////////////////
            //delete old indexfile and replace with new
            //lock

            //keep the old index around just in case
            if (indexfile.exists()){
                indexfile.renameTo(temp);
            }
            //write to tempfile first
            PrintWriter out = new PrintWriter(indexPath);
            out.print(jsonInString);
            out.close();
            temp.delete();
            //////////////////////////////////////////////////////
            Debugger.log("Indexer: Index saved to file", null);

        }
        catch(IOException e){
            Debugger.log("Indexer: Could not write to indexfile!", e);
            return false;
        }
        return(true);
    }
    //add an entry to the index file
    //process thread safe
    public static synchronized boolean addEntry(IndexFile ind, IndexEntry ent){
        //get consent then add
        ind.add(ent);
        ind.indexChunks(ent);
        //-----------
        //update() file on disk
        return true;
    }
    //add an entry to the index file
    //process thread safe
    public static synchronized boolean removeEntry(IndexFile ind, IndexEntry ent){
        //get consent then add
        ind.remove(ent);
        return true;
    }

    public static String serializeUpdate(IndexUpdate update){
        String serialized = null;
        ObjectMapper mapper = new ObjectMapper();
        try{
            serialized =  mapper.writeValueAsString(update);
        }
        catch (IOException e){
            Debugger.log("", e);
        }
        return serialized;
    }

    public static IndexUpdate deserializeUpdate(String update){
        ObjectMapper mapper = new ObjectMapper();
        IndexUpdate temp = null;
        try{
            temp = mapper.readValue(update, IndexUpdate.class);
        }
        catch (Exception e){
            Debugger.log("", e);
        }
        return(temp);
    }

    public static void init(String i_p){
        indexPath = i_p;
    }


}
