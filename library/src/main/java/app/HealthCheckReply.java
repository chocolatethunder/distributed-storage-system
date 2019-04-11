package app;

import java.util.Map;
import java.util.Set;

/**
 *This class is used for creating the Json String for health check reply content
 */
public class HealthCheckReply {

    private String status;
    private long diskSpace;
    private Set<String> corruptedChunks;
    private Module sender;


    public HealthCheckReply(){}

    public HealthCheckReply(Module sender,
                            String status,
                            long diskSpace,
                            Set<String> corruptedChunks){

        this.setSender(sender);
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

    public Set<String> getCorruptedChunks() {
        return corruptedChunks;
    }

    public void setCorruptedChunks(Set<String> corruptedChunks) {
        this.corruptedChunks = corruptedChunks;
    }

    public Module getSender() {
        return sender;
    }

    public void setSender(Module sender) {
        this.sender = sender;
    }
}
