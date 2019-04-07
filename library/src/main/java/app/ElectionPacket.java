package app;

/**
 * Election packet that contains the leader's Uuid and IP
 */
public class ElectionPacket {

    public String uuid = "";
    public String ip = "";

    public ElectionPacket(){}

    public ElectionPacket(String leaderUuid,String leaderIp)
    {
        this.uuid = leaderUuid;
        this.ip = leaderIp;

    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }



}
