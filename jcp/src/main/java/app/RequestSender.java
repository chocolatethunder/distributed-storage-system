package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 *
 */
public class RequestSender {

    Socket socket = null;
    DataOutputStream out = null;
	String lastIPAccessed = "127.0.0.1";



    private static class RequestSenderHolder{

        static final RequestSender requestSender = new RequestSender();
    }

    private RequestSender(){

 

        //TO:DO  need to implement the ROUND ROBIN logic to chose the STALKER to connect
		connectToNextStalker();




    }

    public static RequestSender getInstance(){
        return  RequestSenderHolder.requestSender;
    }



    public void sendFile(String fileName){

        // TO:DO need logic to verify file size here

        if(handShakeSuccess(RequestType.UPLOAD)) {
            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.sendFile(fileName);

        }else{
            //need a way to connect to another STALKER
            //DEBUG
            System.out.println("SERVER BUSY");
        }

    }


    public List<String> getFileList(){


        if(handShakeSuccess(RequestType.LIST)) {



        }

        return null;

    }


    public void deleteFile(String fileName){

        // TO:DO need logic to verify file size  here

        if(handShakeSuccess(RequestType.DELETE)) {



        }

    }

    public void getFile(String filePath){

        // TO:DO need logic to verify file size  here

        if(handShakeSuccess(RequestType.DOWNLOAD)) {

            FileStreamer fileStreamer = new FileStreamer(socket);
            fileStreamer.receiveFile(filePath);

        }

    }




    private boolean handShakeSuccess(RequestType requestType){
        TcpPacket receivedPacket = null;
        try {


            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in = new DataInputStream((socket.getInputStream()));

            TcpPacket initialPacket = new TcpPacket(requestType, "HELLO_INIT");

            ObjectMapper mapper = new ObjectMapper();

            //DEBUG : Object to JSON in file
            //mapper.writeValue(new File("file.json"), initialPacket);

            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);


            try {

                String received = in.readUTF();
                System.out.println("rec " + received);
                receivedPacket = mapper.readValue(received, TcpPacket.class);

            } catch (EOFException e) {
                // do nothing end of packet
            }

        } catch (IOException  e) {
            e.printStackTrace();
        }

        return receivedPacket != null && receivedPacket.getMessage().equals("AVAIL");
    }


    private Socket createConnection(String host, int port) throws IOException {
        Socket socket = null;

        // establish a connection
        //TO:DO Need logic for getting the stalker in round robin fashion
            socket = new Socket(host, port);
            System.out.println("Connected");
        return socket;
    }
	
	public void connectToNextStalker() {
		

			
			//System.out.println(stalkerList);
			
			

            //socket = createConnection("127.0.0.1", 6553);
			//System.out.println(nextIP);
			while (socket == null) {
				try {
					socket = createConnection(getNextIP(), 6553);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	public String getNextIP() {
		//change this to a json file?
		String stalkerListFile = "STALKERs.txt";
		List<String> stalkerList = new ArrayList<String>();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(stalkerListFile));
		
			String readInIP;
			while ((readInIP = in.readLine()) != null) {
				stalkerList.add(readInIP);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int lastIPIndex = stalkerList.indexOf(lastIPAccessed);
		int nextIPIndex = lastIPIndex + 1;
		if (nextIPIndex >= stalkerList.size()) {
			nextIPIndex = 0;
		}
		String nextIP = stalkerList.get(nextIPIndex);
		return nextIP;
	}


}
