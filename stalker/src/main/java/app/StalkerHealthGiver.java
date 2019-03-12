package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class StalkerHealthGiver implements Runnable {
	
	
	@Override
	public void run() {
		
        Socket socket = null;
        ServerSocket server = null;
		
		try {
			server = new ServerSocket(6552);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			try {
				socket = server.accept();
				System.out.println("Accepted connection: " + socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}