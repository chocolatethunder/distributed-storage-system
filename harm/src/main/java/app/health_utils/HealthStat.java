package app.health_utils;


import app.Debugger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

// Core logics of health checkups
// This class is a Singleton
public class HealthStat{

    // Singleton obj
    private static HealthStat health_instance = null;
    // { ChunkName(GUIDs), Hash }
    private Map<String,String> corruptList;

    private HealthStat(){
        corruptList = new HashMap<>();
    }

    private void refresh(){
        corruptList = new HashMap<>();
    }

    // Function to be called for periodic health check
    // preferably once every 1 minute, this can get pretty intensive computational-power wise
    public void healthCheck(HashIndex ind){
        Debugger.log("Commencing Self Diagnosis", null);
        refresh();

        Map<String, String> entries = ind.getEntries();
        // Looping through the entries and checking the files
        for (Map.Entry<String, String> entry : entries.entrySet()){
            // Opening File
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                // shouldn't arrive here
                e.printStackTrace();
            }
            try (InputStream is = Files.newInputStream(Paths.get(entry.getKey()));
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                int i;
                while ((i = dis.read()) != -1){
                    // read through the file & update digest
                }
            }
            catch (IOException e){
                //if the file is corrupt or empty, report as corrupt
                e.printStackTrace();
                Debugger.log(entry.getKey() + " is inaccessible.",null);

            }
            // Check digest
            String hash = md.digest().toString();
            if(hash == entry.getValue()){
                // eq, good  hash do nothing (?)
            }else{
                // corrupted, add to list
                Debugger.log(entry.getKey() + " is corrupted, adding to corrupted list.",null);
                corruptList.put(entry.getKey(),entry.getValue());
            }
        }
    }

    // Function to be called for Health Report
    // returns a nicely formatted string for report
    public String status(){
//        System.out.println("Requesting Health Report");
        ObjectMapper mapper = new ObjectMapper();
        String strRes = "";
        if(corruptList.isEmpty()) {
            // All Green
            HealthResponse response = new HealthResponse("GOOD");
            try {
                strRes = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("Error generating response message.");
            }
            return strRes;
        }
        // Corruption detected
        HealthResponse response = new HealthResponse("CORR", corruptList.keySet());
        try {
            strRes = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("Error generating response message.");
        }
        return strRes;
    }


    ////////////////////getter/setter

    public Map<String, String> getCorruptList() {return corruptList; }

    // call to get instance
    public static HealthStat getInstance(){
        if(health_instance == null) health_instance = new HealthStat();
        return health_instance;
    }


}
