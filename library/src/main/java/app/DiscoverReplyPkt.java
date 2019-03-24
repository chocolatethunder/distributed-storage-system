package app;

import java.net.InetAddress;

public class DiscoverReplyPkt {
    private String type;
    private String sender;
    private String uuid;
    private String address;

    public DiscoverReplyPkt(String type, String sender, String uuid, String address) {
        this.type    = type;
        this.address = address;
        this.sender  = sender;
        this.uuid    = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
