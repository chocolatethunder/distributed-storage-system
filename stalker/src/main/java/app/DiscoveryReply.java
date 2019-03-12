package app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class DiscoveryReply
{
	private DatagramSocket socket;
	private boolean running;
	private static int STALKERPORT = 5000;
	private byte[] req = new byte[1024];

	public DiscoveryReply() {
		socket = new DatagramSocket(STALKERPORT);
	}

	public void listen() {
		running = true;

		while (running) {
			
			DatagramPacket packet = new DatagramPacket(req, req.length);
			socket.receive(packet);
			
			// get Info about the packet
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			
			// send respose to the server
			packet = new DatagramPacket(req, req.length, address, port);
			String received = new String(packet.getData(), 0, packet.getLength());

			if (received.equals("end")) {
				running = false;
				continue;
			}
			socket.send(packet);
		}
		socket.close();
	}

}