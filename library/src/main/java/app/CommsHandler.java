package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.Optional;
import java.net.Socket;

public class CommsHandler {

    public CommsHandler() {
    }

    //Send a tcp packet on a designated socket
    //if a request is being made it should be serialized JSON string of a request
    public MessageType sendPacket(Socket socket, MessageType messageType, String content, boolean ack) {
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
            //Debugger.log(jsonInString, null);
            if (ack) {
                try {
                    // receiving packet back from STALKER
                    String received = in.readUTF();
                    //Debugger.log("Comm Link: received packet " + received, null);
                    receivedPacket = mapper.readValue(received, TcpPacket.class);
                    response = receivedPacket.getMessageType();
                } catch (EOFException e) {
                    Debugger.log("EOF", null);
                    // do nothing end of packet
                }
            }

        } catch (IOException e) {
            Debugger.log("socket timed out", null);
        }
        return response;
    }

    //function for recieving a TCP packet once a connection has been established
    public TcpPacket receivePacket(Socket socket) {
        TcpPacket recieved = new TcpPacket(MessageType.BUSY, "");;
        ObjectMapper mapper = new ObjectMapper();
        Optional<TcpPacket> receivedPacket = Optional.empty();

        try {
            //read string from port
            String rec = new DataInputStream(socket.getInputStream()).readUTF();
            // reading the packet as object from json string
            recieved =  Optional.of(mapper.readValue(rec, TcpPacket.class)).get();


        }
        catch (Exception e) {
            Debugger.log("Comms Handler: packet could not be received", null);
        }
        return recieved;
    }

    public boolean sendResponse(Socket socket, MessageType messageType) {
        return (sendResponse(socket, messageType, ""));
    }

    //send a response packet on a socket stream
    public boolean sendResponse(Socket socket, MessageType messageType, String message) {
        ObjectMapper mapper = new ObjectMapper();
        TcpPacket sendAvail = new TcpPacket(messageType, message);
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(sendAvail);
            //Debugger.log("Comm Link: response: " + jsonInString, null);
            //write packet to port
            new DataOutputStream(socket.getOutputStream()).writeUTF(jsonInString);
        }
        catch (SocketException e){
            Debugger.log("Could not send response: socket closed.", e);
        }
        catch (IOException e) {
            Debugger.log("", e);
        }
        return (true);
    }

    //must fix now
    public boolean sendRequestToLeader(MessageType m) {
        try {
            //connect to leader and send request
            //currently hard coded
            ConfigFile cfg = ConfigManager.getCurrent();
            Map<Integer, String> slist = NetworkUtils.getStalkerMap(cfg.getStalker_list_path());
            Socket leader = NetworkUtils.createConnection(slist.get(cfg.getLeader_id()), cfg.getLeader_report());
            if (!(sendPacket(leader, m, "", true) == MessageType.ACK)) {
                return false;
            } else {
                //everything worked out
                //close the connection
                leader.close();
                return true;
            }
        } catch (IOException e) {
            Debugger.log("", e);
            //should have another way to deal with failure here...
            return false;
        }
        ///////////////////////////////
        //Wait for leader to grant permission to start
    }

    public Socket getLeaderResponse(int server_port) {
        TcpPacket req;
        ServerSocket listener;
        Socket leader;
        try {
            listener = new ServerSocket(server_port);
            leader = listener.accept();
            Debugger.log("Comm Link: Connected to leader: ", null);
            System.out.println(NetworkUtils.timeStamp(1) + "Connected to leader: ");
            req = receivePacket(leader);
            Debugger.log("Comm Link: Permission from leader granted", null);
            listener.close();
        } catch (IOException e) {
            Debugger.log("Comm Link: Could not use server port", e);
            return (null);
        }
        if (!(req.getMessageType() == MessageType.START)) {
            Debugger.log("Comm Link: Request denied by leader.", null);
            return (null);
        }
        return (leader);
    }

    /**
     * This method only sends a packet does not wait for reply
     *
     * @param socket
     * @param messageType
     * @param content
     * @return
     */
    public MessageType sendPacketWithoutAck(Socket socket, MessageType messageType, String content) {

        MessageType response = null;
        try {
            TcpPacket initialPacket = new TcpPacket(messageType, content);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream((socket.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();
            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(initialPacket);
            out.writeUTF(jsonInString);
        } catch (IOException e) {
            Debugger.log("", e);
        }
        return response;
    }


}
