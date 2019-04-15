package app;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sun.security.krb5.Config;


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
            //only bind your ports once!!!
            receiverSocket = new DatagramSocket(ports[1]);
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
        }
        catch (SocketException e){
        }
        int counter = 1;
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown()){
            try {
                listOfAddrs = serverSearch(MessageType.DISCOVER, ports);
                if (listOfAddrs != null){
                    if (target == Module.STALKER.name()){
                        //write to file
                        if (counter > 5){
                            Debugger.log("NetDiscovery: STALKER list updated", null);
                            counter = 1;
                        }

                        NetworkUtils.toFile(ConfigManager.getCurrent().getStalker_list_path() , listOfAddrs);
                    }
                    else{
                        if (counter > 5){
                            Debugger.log("NetDiscovery: HARM list updated", null);
                            counter = 1;
                        }
                        //write to file

                        HashMap<Integer, NodeAttribute> n = new HashMap<>();
                        for (int key : listOfAddrs.keySet()){
                            NodeAttribute node = new NodeAttribute();
                            node.setAddress(listOfAddrs.get(key));
                            node.setAlive(true);
                            node.setSpace(0);
                            n.put(key, node);
                        }

                        NetworkUtils.toFile(ConfigManager.getCurrent().getHarm_list_path(), n);
                    }

                }
            } catch (Exception e) {
                Debugger.log("", e);
            }
            try{Thread.sleep(discovery_timeout * 1000);}catch (Exception e){};
        }
        Debugger.log("NetDiscovery: Shutdown OK!", null);

    }

    /**
     * Takes in RequestType and target as arguments
     * broadcasts a UDP packet over a LAN network
     * @return list of address of the modules that repllied
     */
    public HashMap<Integer, String> serverSearch(MessageType request,int[] ports) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try{
            if(sendSignal(request, mapper)){
                HashMap<Integer,String> m;
                m = receiveSignal(mapper);
                if(m != null){
                    return(m);
                }
            }
        }
        catch (IOException e){
            Debugger.log("NetDiscovery: Server search failed!", e);
        }
        return (null);

    }

    //send out a UDP broadcast
    public boolean sendSignal(MessageType request, ObjectMapper mapper) throws IOException{
        // To broadcast change this to 255.255.255.255
        InetAddress address = InetAddress.getByName(ConfigManager.getCurrent().getBroadcast_ip());       // broadcast address
        //we want a map of MAC -> ip
        //the ports must be specific to target/origin
        int[] ports = NetworkUtils.getPortTargets(origin, target);
        // create a discover request packet and broadcast it
        UDPPacket discovery = new UDPPacket(request, String.valueOf(NetworkUtils.getMacID()), target, NetworkUtils.getIP());
        if (verbose){ Debugger.log("NetDiscovery: Sending out broadcast with signature: " + mapper.writeValueAsString(discovery), null);}
        byte[] req = mapper.writeValueAsString(discovery).getBytes();
        //the port we are sending on
        DatagramPacket packet = new DatagramPacket(req, req.length, address, ports[0]);
        socket.send(packet);
        return(true);
    }

    public HashMap<Integer, String> receiveSignal(ObjectMapper mapper){
        HashMap<Integer, String> stalkerMap = new HashMap<>();
        // waits for 5 sec to get response from the LAN
        long t= System.currentTimeMillis();
        long end = t + (discovery_timeout*1000);
        while(System.currentTimeMillis() < end) {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            // set socket timeout to 1 sec
            try {
                receiverSocket.setSoTimeout(discovery_timeout);
                receiverSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                //if (verbose) { Debugger.log("NetDiscovery: A target has responded: " + received, null);}

                // parse the packet content
                JsonNode discoverReply = mapper.readTree(received);
                String uuid = discoverReply.get("uuid").textValue();
                InetAddress replyAddress =  InetAddress.getByName(discoverReply.get("address").textValue());
                stalkerMap.put(Integer.valueOf(uuid), replyAddress.getHostAddress());


                // need to have the harm list to have additional attributes such as space, and if alive
                // during health check, the space and alive will be updated
                if(this.target.equals(Module.HARM.name())){
                    NodeAttribute attributes = new NodeAttribute(replyAddress.getHostAddress(), 0, true);
                    String stringAttributes = mapper.writeValueAsString(attributes);
                    stalkerMap.put(Integer.valueOf(uuid), stringAttributes);
                }else {
                    stalkerMap.put(Integer.valueOf(uuid), replyAddress.getHostAddress());
                }
            }
            catch (SocketTimeoutException ex)
            {
            }
            catch (IOException e){
                Debugger.log("", e);
            }
        }
        //socket.close();
        return stalkerMap;
    }



}
