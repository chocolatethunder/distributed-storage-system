package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.*;

public class DiscoveryReply implements Runnable {

    private DatagramSocket socket;
    private byte[] req = new byte[1024];
    private static int STALKERPORT = 10000;
    private static int JCPPORT = 11000;
    private static int listening_timeout;
    Module module;


    public DiscoveryReply(Module module, int listening_timeout) {
        this.module = module;
        this.listening_timeout = listening_timeout;
        try {
            socket = new DatagramSocket(STALKERPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // runs for 15 sec for a udp broadcast
        while (true)
        {
            DatagramPacket packet = new DatagramPacket(req, req.length);
            try {
                socket.setSoTimeout(listening_timeout*1000);
                socket.receive(packet);
            }catch (SocketTimeoutException e)
            {
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            // parse the packet
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("A discovery probe was received: " + received);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode request = null;
            String req_type = null;
            String req_sender = null;
            String req_target = null;
            String req_address = null;
            try {
                request = mapper.readTree(received);
                req_type = request.get("type").textValue();
                req_target = request.get("target").textValue();
                req_sender = request.get("sender").textValue();
                req_address = request.get("address").textValue();

            } catch (IOException e) {
                e.printStackTrace();
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
                reply = new UDPPacket(m, module.name(),String.valueOf(NetworkUtils.getMacID()), NetworkUtils.getIP());
                byte[] req = new byte[0];
                try {
                    req = mapper.writeValueAsString(reply).getBytes();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                DatagramPacket replyPkt = new DatagramPacket(req, req.length, address, JCPPORT);
                try {
                    socket.send(replyPkt);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        socket.close();

    }

}
