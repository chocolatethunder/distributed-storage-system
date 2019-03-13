package app;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
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
            ind = Optional.of(mapper.readValue(fileToString(indexPath), IndexFile.class));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return ind.get();
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
    public static boolean addEntry(IndexFile ind, IndexEntry ent){
        //get consent then add
        //ind.add();
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
        }
        return fileString;
    }
}
