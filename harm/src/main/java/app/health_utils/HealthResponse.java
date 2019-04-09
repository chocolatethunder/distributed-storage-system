package app.health_utils;

import java.io.File;
import java.util.Set;

// This class is created solely to be used to convert into JSON string for nicer formatting
public class HealthResponse {
    private String status;
    private long avail_space;
    private Set<String> chunks;


    public HealthResponse(String status){
        this.setStatus(status);
        long free = new File("/").getUsableSpace();
        this.setAvail_space(free);
    }

    public HealthResponse(String status, Set<String> chunks){
        this.setStatus(status);
        long free = new File("/").getUsableSpace();
        this.setAvail_space(free);
        this.setChunks(chunks);
    }

    public String getStatus(){ return status;}
    public long getAvail_space(){ return avail_space;}
    public Set<String> getChunks(){ return chunks;}
    public void setStatus(String status){ this.status = status;}
    public void setAvail_space(long avail_space){ this.avail_space = avail_space;}
    public void setChunks(Set<String> chunks){ this.chunks = chunks;}
}
