package app;

import java.io.Serializable;
import java.net.InetAddress;

public class UdpPacket {
	private String type;
	private String target;
	private InetAddress address;
	
	public UdpPacket(RequestType requestType, String target, InetAddress address){

        this.type = requestType.name();
        this.target =  target;
        this.address = address;
    }
	
	public String getType() {
		return this.type;
	}
	public String getTarget() {
		return this.target;
	}
	public InetAddress getAddress() {
		return this.address;
	}

}