package app;

public class UDPPacket {

    private String type;
    private String uuid;
    private String target;
    private String address;

    public UDPPacket(MessageType requestType, String uuid, String target, String address){

        this.type = requestType.name();
        this.uuid = uuid;
        this.target =  target;
        this.address = address;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
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
