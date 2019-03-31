package app;

public class ElectionPacket {

    public String leaderUuid= "";
    public String leaderIp = "";

    public ElectionPacket(String leaderUuid,String leaderIp)
    {
        this.leaderUuid = leaderUuid;
        this.leaderIp = leaderIp;

    }

    public String getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(String leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public String getLeaderIp() {
        return leaderIp;
    }

    public void setLeaderIp(String leaderIp) {
        this.leaderIp = leaderIp;
    }



}
