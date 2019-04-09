package app.health_utils;


import app.NetworkUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

//This class will be in control of the indexFiles
//all these functions are probably going to need to be thread safe
// =============================================
// Re-used Stalker units' indexfile logics
// =============================================
public class Indexer {
    public static final String indexPath = "index/main.index";
    //loads the json from file and converts it to an IndexFile object
    public static IndexFile loadFromFile(){
        ObjectMapper mapper = new ObjectMapper();
        Optional<IndexFile> ind = Optional.empty();
        try {
            File f = new File(indexPath);
            if (!f.exists()){
                throw new FileNotFoundException("Index file not found...");
            }
            ind = Optional.of(mapper.readValue(NetworkUtils.fileToString(indexPath), IndexFile.class));
        }
        catch (IOException e){
            //if the file is corrupt or empty we create a new IndexFile
            e.printStackTrace();
            System.out.println("Creating new indexfile");
            IndexFile temp = new IndexFile();
            saveToFile(temp);
            return(temp);
        }
        System.out.println("Indexfile loaded from file.");
        return ind.get();
    }

    //Save index to file
    public static boolean saveToFile(IndexFile ind){
        String tempfile = "index/main";
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
            System.out.println("Index saved to file");

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Could not write to indexfile!");
            return false;
        }
        return(true);
    }



//    //prints the indexfile object to file
//    public static boolean writeIndex(IndexFile ind){
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            //Object to JSON in String
//            String jsonInString = mapper.writeValueAsString(ind);
//
//        }
//        catch(IOException e){
//            e.printStackTrace();
//        }
//        return true;
//    }

    //add an entry to the index file
    //process thread safe
    public static boolean addEntry(IndexFile ind, String id, String hash){
        //get consent then add
        ind.add(id,hash);
        //-----------
        //update() file on disk
        return true;
    }

}
