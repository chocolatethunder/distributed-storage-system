package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import sun.plugin2.message.Message;

import java.io.*;
import java.util.Optional;
import java.net.Socket;

public class CommsHandler {

    public CommsHandler(){}
    //Send a tcp packet on a designated socket
    //if a request is being made it should be serialized JSON string of a request
    public MessageType sendPacket(Socket socket, MessageType messageType, String content){
        TcpPacket receivedPacket = null;
        MessageType response = null;
        try {
            TcpPacket initialPacket = new TcpPacket(messageType, content);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream((socket.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();

            //DEBUG : Object to JSON in file
            //mapper.writeValue(new File("file.json"), initialPacket);
            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);
            try {
                // receiving packet back from STALKER
                String received = in.readUTF();
                System.out.println("rec " + received);
                receivedPacket = mapper.readValue(received, TcpPacket.class);
                response = receivedPacket.getMessageType();
            } catch (EOFException e) {
                // do nothing end of packet
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }
        return response;
    }


    //function for recieving a TCP packet once a connection has been established
    public TcpPacket recievePacket(Socket socket) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Optional<TcpPacket> receivedPacket = Optional.empty();

        try {
            //read string from port
            String rec = new DataInputStream(socket.getInputStream()).readUTF();
            // reading the packet as object from json string
            receivedPacket = Optional.of(mapper.readValue(rec, TcpPacket.class));
        }
        catch (EOFException e) {
        }
        return receivedPacket.get();
    }

    //send a response packet on a socket stream
    public boolean sendResponse(Socket socket, MessageType messageType){
        ObjectMapper mapper = new ObjectMapper();
        TcpPacket sendAvail = new TcpPacket(messageType, "AVAIL");
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(sendAvail);
            System.out.println(jsonInString);
            //write packet to port
            new DataOutputStream(socket.getOutputStream()).writeUTF(jsonInString);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return(true);
    }


}