package app;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class NetDiscovery implements Runnable{
    private static DatagramSocket socket = null;
    private DatagramSocket receiverSocket;
    private String target;
    private String origin;
    private static int discovery_timeout = 0;
    private boolean verbose;
    public NetDiscovery(Module target, Module origin, int discovery_timeout, boolean verbose) {
        this.target = target.name();
        this.origin = origin.name();
        this.discovery_timeout = discovery_timeout;
        this.verbose = verbose;

    }

    @Override
    public void run() {
        HashMap<Integer,String> listOfAddrs =  null;
        int[] ports = NetworkUtils.getPortTargets(origin, target);
        try {
            receiverSocket = new DatagramSocket(ports[1]);
        }
        catch (SocketException e){

        }



        while (true){
            try {
                listOfAddrs = broadcast(MessageType.DISCOVER,target);
                if (listOfAddrs != null){
                    if (target == Module.STALKER.name()){
                        //write to file
                        System.out.println("Updating STALKER list");
                        NetworkUtils.toFile("config/stalkers.list", listOfAddrs);
                    }
                    else{
                        //write to file
                        System.out.println("Updating HARM list");
                        NetworkUtils.toFile("config/harm.list", listOfAddrs);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Takes in RequestType and target as arguments
     * broadcasts a UDP packet over a LAN network
     * @return list of address of the modules that repllied
     */
    public HashMap<Integer, String> broadcast(MessageType request,String target) throws IOException {
        // To broadcast change this to 255.255.255.255
        InetAddress address = InetAddress.getByName("192.168.1.255");       // broadcast address
        //we want a map of MAC -> ip
        HashMap<Integer, String> stalkerMap = new HashMap<>();
        socket = new DatagramSocket();
        //the ports must be specific to target/origin
        int[] ports = NetworkUtils.getPortTargets(origin, target);


        //port we are receiving on0
        //breaking here????????!!!
            // socket to receive replies

        // create a discover request packet and broadcast it
        UDPPacket discovery = new UDPPacket(request, String.valueOf(NetworkUtils.getMacID()), target, NetworkUtils.getIP());
        ObjectMapper mapper = new ObjectMapper();
        if (verbose){System.out.println("Sending out broadcast with signature: " + mapper.writeValueAsString(discovery) + "\n");}
        byte[] req = mapper.writeValueAsString(discovery).getBytes();
        //the port we are sending on
        DatagramPacket packet = new DatagramPacket(req, req.length, address, ports[0]);
        socket.send(packet);

        // waits for 5 sec to get response from the LAN
        long t= System.currentTimeMillis();
        long end = t + (discovery_timeout*1000);
        while(System.currentTimeMillis() < end) {
            byte[] buf = new byte[1024];
            packet = new DatagramPacket(buf, buf.length);
            // set socket timeout to 1 sec
            try {
                receiverSocket.setSoTimeout(1000);
                receiverSocket.receive(packet);
            }
            catch (SocketTimeoutException e)
            {
                socket.close();
                return null;
            }
            String received = new String(packet.getData(), 0, packet.getLength());
            if (verbose) {System.out.println("A target has responded: " + received);}
            // parse the packet content
            JsonNode discoverReply = mapper.readTree(received);
            String uuid = discoverReply.get("uuid").textValue();
            InetAddress replyAddress =  InetAddress.getByName(discoverReply.get("address").textValue());
            stalkerMap.put(Integer.valueOf(uuid), replyAddress.getHostAddress());
        }
        socket.close();
        return stalkerMap;
    }
}
