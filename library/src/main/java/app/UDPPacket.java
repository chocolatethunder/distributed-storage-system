package app;

public class UDPPacket {
    private String type;
    private String sender;
    private String target;
    private String address;

    public UDPPacket(MessageType requestType, String sender, String target, String address){

        this.type = requestType.name();
        this.sender = sender;
        this.target =  target;
        this.address = address;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
