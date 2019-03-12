package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class JcpHealthChecker implements Runnable {
	
	List<String> listOfStalkers = new ArrayList<String>();
	
	private Socket socket;
	
	@Override
	public void run() {
		while(true) {
			for (int i = 0; i < 256; i++) {
				//String host = "192.168.1." + i;
				String host = "127.0.0." + i;
				//System.out.println("Attempting to connect to " + host + " ...");
				try {
					socket = new Socket();
					if (listOfStalkers.contains(host)) {
						listOfStalkers.remove(listOfStalkers.indexOf(host));
					}
					socket.connect(new InetSocketAddress(host, 6552), 100);
					listOfStalkers.add(host);
					socket.close();
				} catch (IOException e) {
					//System.out.println("Not connected.");
				}
			}
			
			try {
				FileWriter writer = new FileWriter("STALKERs.txt");
				for(String str: listOfStalkers) {
					writer.write(str+"\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}