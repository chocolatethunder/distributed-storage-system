package app.chunk_utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import app.NetworkUtils;

//This class will be in control of the indexFiles
//all these functions are probably going to need to be thread safe
public class Indexer {
    private static volatile IndexFile index;
    public static final String indexPath = "index/main.index";

    //loads the json from file and converts it to an IndexFile object
    public static synchronized void loadFromFile(){
        ObjectMapper mapper = new ObjectMapper();
        Optional<IndexFile> ind = Optional.empty();
        try {
            File f = new File(indexPath);
            if (!f.exists()){
                throw new FileNotFoundException("Index file not found...");
            }
            ind = Optional.of(mapper.readValue(NetworkUtils.fileToString(indexPath), IndexFile.class));
            System.out.println("Indexfile loaded from file.");
        }
        catch (IOException e){
            //if the file is corrupt or empty we create a new IndexFile
            e.printStackTrace();
            System.out.println("Creating new indexfile");
            IndexFile temp = new IndexFile();
            index = temp;
            //return(temp);
        }
        //return ind.get();
    }

    //Save index to file
    public static synchronized boolean saveToFile(){
        String tempfile = "index/main";
        try{
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(index);

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
            System.out.println("Index saved to file");

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Could not write to indexfile!");
            return false;
        }
        return(true);
    }
    //add an entry to the index file
    //process thread safe
    public static synchronized boolean addEntry(IndexEntry ent){
        //get consent then add
        index.add(ent);
        index.indexChunks(ent);
        //-----------
        //update() file on disk
        return true;
    }
    //add an entry to the index file
    //process thread safe
    public static synchronized boolean removeEntry(IndexEntry ent){
        //get consent then add
        index.remove(ent);
        return true;
    }

    //add an entry to the index file
    //process thread safe
    public static synchronized IndexEntry search(String id){
        //get consent then add
        return(index.search(id));
    }
    public static synchronized List<String> fileList(){
        return(index.fileList());
    }

}
