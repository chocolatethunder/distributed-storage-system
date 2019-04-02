package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.*;

public class DiscoveryReply implements Runnable {

    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] req = new byte[1024];
    private final int STK_JCP = 10000;
    private final int JCP_STK = 11000;

    private final int STK_HARM = 10001;
    private final int HARM_STK = 11001;

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
                socket = new DatagramSocket(ports[0]);
                socket.setSoTimeout(listening_timeout * 1000);
                bound = true;
            } catch (Exception e) {
                //e.printStackTrace();

            }
        }
        while(!Thread.interrupted()){
            listen();
        }
    }

    public void listen(){
        String req_target = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            socket.receive(packet);
            // parse the packet
            String received = new String(packet.getData(), 0, packet.getLength());
            if(verbose){System.out.println("A discovery probe was received: " + received);}
            JsonNode request = null;
            request = mapper.readTree(received);
            req_target = request.get("target").textValue();

        }catch (SocketTimeoutException e)
        {
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
                e.printStackTrace();
                return;
            }
            DatagramPacket replyPkt = new DatagramPacket(req, req.length, address, ports[1]);
            try {
                socket.send(replyPkt);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
