package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.*;

public class DiscoveryReply implements Runnable {

    private DatagramSocket receiverSocket;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] req = new byte[1024];
    private boolean verbose;
    private static int listening_timeout;
    Module module;
    private int[] ports;

    //listen for packets from a certain module
    //module is listening module, expected is the one being listened for
    public DiscoveryReply(Module module, Module expected, int listening_timeout, boolean verbose) {
        this.module = module;
        this.ports = NetworkUtils.getPortTargets(expected.name(), module.name());
        this.listening_timeout = listening_timeout;
        this.verbose = verbose;
    }

    @Override
    public void run() {
        // runs for 15 sec for a udp broadcast
        packet = new DatagramPacket(req, req.length);
        boolean bound = false;
        while (!bound){
            try {
                //where to listen
                //breaks here...
                receiverSocket = new DatagramSocket(ports[0]);
                socket = new DatagramSocket();
                receiverSocket.setSoTimeout(listening_timeout * 1000);
                bound = true;
            } catch (Exception e) {
                Debugger.log("", e);
            }
        }
        while(!Thread.interrupted()){
            listenServer();
        }

    }

    public void listenServer(){
        ObjectMapper mapper = new ObjectMapper();
        String req_targ = receiveSignal(mapper);
        if (req_targ != null){
            sendSignal(mapper, req_targ);
        }

    }

    public String receiveSignal(ObjectMapper mapper){
        String req_target = null;
        try {
            receiverSocket.receive(packet);
            // parse the packet
            String received = new String(packet.getData(), 0, packet.getLength());
            if(verbose){Debugger.log("DiscReply: A discovery probe was received: " + received, null);}
            JsonNode request = null;
            request = mapper.readTree(received);
            req_target = request.get("target").textValue();

        }catch (SocketTimeoutException e)
        {
            //Debugger.log("", e);
        }
        catch (IOException e) {
            Debugger.log("", e);
            return null;
        }
        return req_target;
    }

    public boolean sendSignal(ObjectMapper mapper, String req_target){
        // Make sure the broadcast is targeting this module
        if(req_target.equals(module.name()))
        {
            // get Info about the packet
            InetAddress address = packet.getAddress();
            // make the response JSON
            UDPPacket reply = null;
            //we must designate what type of device is responding to the message
            MessageType m;
            switch(module){
                case HARM:
                    m = MessageType.DISC_HARM_R;
                    break;
                case STALKER:
                    m = MessageType.DISC_STK_R;
                    break;
                default:
                    m = MessageType.ERROR;
                    break;
            }
            reply = new UDPPacket(m, String.valueOf(NetworkUtils.getMacID()), module.name(), NetworkUtils.getIP());
            //byte[] req = new byte[0];
            try {
                req = mapper.writeValueAsString(reply).getBytes();
            } catch (JsonProcessingException e) {
                Debugger.log("", e);
                return false;
            }
            DatagramPacket replyPkt = new DatagramPacket(req, req.length, address, ports[1]);
            try {
                socket.send(replyPkt);
            } catch (IOException e) {
                Debugger.log("", e);
                return false;
            }
        }
        return true;
    }

}
