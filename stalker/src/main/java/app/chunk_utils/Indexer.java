package app.chunk_utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

//This class will be in control of the indexFiles
//all these functions are probably going to need to be thread safe
public class Indexer {

    //loads the json from file and converts it to an IndexFile object
    public static IndexFile loadFromFile(String indexPath){
        ObjectMapper mapper = new ObjectMapper();
        Optional<IndexFile> ind = Optional.empty();
        try {
            File f = new File(indexPath);
            if (!f.exists()){
                throw new FileNotFoundException("Index file not found...");
            }
            ind = Optional.of(mapper.readValue(fileToString(indexPath), IndexFile.class));
        }
        catch (IOException e){
            //if the file is corrupt or empty we create a new IndexFile
           // e.printStackTrace();
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
        try{
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(ind);

            File f = new File("index/main");
            if (f.exists()){
                f.delete();
            }
            PrintWriter out = new PrintWriter("index/main");
            out.print(jsonInString);
            out.close();

            
            System.out.println("Index saved to file");

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Could not write to indexfile!");
            return false;
        }
        return(true);
    }



    //prints the indexfile object to file
    public static boolean writeIndex(IndexFile ind){
        try {
            ObjectMapper mapper = new ObjectMapper();
            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(ind);

        }
        catch(IOException e){
            e.printStackTrace();
        }
        return true;
    }

    //add an entry to the index file
    //process thread safe
    public static boolean addEntry(IndexFile ind, IndexEntry ent){
        //get consent then add
        ind.add(ent);
        //-----------
        //update() file on disk
        return true;
    }

    private static String fileToString(String fileName) {
        String fileString = "";
        try{
            fileString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        }
        catch (IOException e){
            e.printStackTrace();
            return(null);
        }
        catch (NullPointerException ex){
            return(null);
        }
        return fileString;
    }
}
