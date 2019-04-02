package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.*;

public class DiscoveryReply implements Runnable {

    private DatagramSocket socket;
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

        while(true){
            try {
                //where to listen
                socket = new DatagramSocket(ports[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatagramPacket packet = new DatagramPacket(req, req.length);
            try {
                socket.setSoTimeout(listening_timeout*1000);
                socket.receive(packet);
            }catch (SocketTimeoutException e)
            {
                socket.close();
                return;
            }
            catch (IOException e) {
                e.printStackTrace();
                socket.close();
                return;
            }

            // parse the packet
            String received = new String(packet.getData(), 0, packet.getLength());
            if(verbose){System.out.println("A discovery probe was received: " + received);}
            ObjectMapper mapper = new ObjectMapper();
            JsonNode request = null;
            String req_type = null;
            String req_target = null;
            String req_address = null;
            try {
                request = mapper.readTree(received);
                req_type = request.get("type").textValue();
                req_target = request.get("target").textValue();
                req_address = request.get("address").textValue();

            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
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
                byte[] req = new byte[0];
                try {
                    req = mapper.writeValueAsString(reply).getBytes();
                } catch (JsonProcessingException e) {
                    socket.close();
                    e.printStackTrace();
                    return;
                }
                DatagramPacket replyPkt = new DatagramPacket(req, req.length, address, ports[1]);
                try {
                    socket.send(replyPkt);
                } catch (IOException e) {
                    socket.close();
                    e.printStackTrace();
                    return;
                }


            }
        }

        //socket.close();

    }

}
