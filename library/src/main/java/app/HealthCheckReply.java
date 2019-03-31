package app;

import java.util.Map;

/**
 *This class is used for creating the Json String for health check reply content
 */
public class HealthCheckReply {

    private String status;
    private long diskSpace;
    private Map<Integer, Integer> corruptedChunks;


    public HealthCheckReply(){}

    public HealthCheckReply(String status, long diskSpace, Map<Integer, Integer> corruptedChunks){

        this.setStatus(status);
        this.setDiskSpace(diskSpace);
        this.setCorruptedChunks(corruptedChunks);

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(long diskSpace) {
        this.diskSpace = diskSpace;
    }

    public Map<Integer, Integer> getCorruptedChunks() {
        return corruptedChunks;
    }

    public void setCorruptedChunks(Map<Integer, Integer> corruptedChunks) {
        this.corruptedChunks = corruptedChunks;
    }
}
