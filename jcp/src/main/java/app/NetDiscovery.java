package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetDiscovery{
	
	private static DatagramSocket socket = null;
	private static int STALKERPORT = 5000;
	private static InetAddress myIP = inetAddress.getHostAddress();
	private static final InetAddress BROADCASTADDRESS = InetAddress.getByName("255.255.255.255");
	
	
	/**
	 * Floods the whole LAN with identify network discovery packets,
	 * and listen for reply from servers,  
	 */
	public static void netDiscovery() throws IOException 
	{
		// init socket
		socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		// req structure 
        UdpPacket discovery = new UdpPacket(RequestType.DISCOVER, "master", myIP);
        byte[] req = mapper.writeValueAsString(initialPacket).getBytes();
	    
	    // send out the packet
	    DatagramPacket packet = new DatagramPacket(req, req.length, BROADCASTADDRESS, STALKERPORT);
		socket.send(packet);
		
		// wait for the response
		byte[] buf = new Byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		
		System.out.print(packet.getData().toString());
		
		socket.close();
	}
