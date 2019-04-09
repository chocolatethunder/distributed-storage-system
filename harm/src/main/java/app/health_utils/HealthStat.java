package app.health_utils;


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
public class HealthStat{

    // { ChunkName(GUIDs), Hash }
    private Map<String,String> corruptList;

    public HealthStat(){
        corruptList = new HashMap<>();
    }

    private void refresh(){
        corruptList = new HashMap<>();
    }

    // Function to be called for periodic health check
    // preferably once every 1 minute, this can get pretty intensive computational-power wise
    public void healthCheck(IndexFile ind){
        System.out.println("Commencing Health Check");
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
                System.out.println(entry.getKey() + " is inaccessible.");

            }
            // Check digest
            String hash = md.digest().toString();
            if(hash == entry.getValue()){
                // eq, good  hash do nothing (?)
            }else{
                // corrupted, add to list
                System.out.println(entry.getKey() + " is corrupted, adding to corrupted list.");
                corruptList.put(entry.getKey(),entry.getValue());
            }
        }
    }

    // Function to be called for Health Report
    // returns a nicely formatted string for report
    public String status(){
        System.out.println("Requesting Health Report");
        ObjectMapper mapper = new ObjectMapper();
        String strRes = "";
        if(corruptList.isEmpty()) {
            // All Green
            HealthResponse response = new HealthResponse("GOOD");
            try {
                strRes = mapper.writeValueAsString(response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("Error generating response message.");
            }
            System.out.println(strRes);
            return strRes;
        }
        // Corruption detected
        HealthResponse response = new HealthResponse("CORR", corruptList.keySet());
        try {
            strRes = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("Error generating response message.");
        }
        System.out.println(strRes);
        return strRes;
    }


    ////////////////////getter/setter

    public Map<String, String> getCorruptList() {return corruptList; }
    public void setCorruptList(Map<String, String> corruptList) { this.corruptList = corruptList; }



}
