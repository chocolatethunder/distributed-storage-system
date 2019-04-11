package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.server.ExportException;
import java.util.*;


public class LeaderCheck {

    private static HashMap<Integer, String> stalkerMap;   // stalker list
    private static List<Integer> ids = new ArrayList<>();                   // stalker uuids

    private static int leaderUuid = Integer.MAX_VALUE;
    private static String leaderIP = "x.x.x.x";
    private static ConfigFile cfg;
    public LeaderCheck()
    {
        cfg = ConfigManager.getCurrent();
        this.ids = NetworkUtils.getStalkerList(cfg.getStalker_list_path());
    }


    public void election(int mode)
    {
        stalkerMap = updateStalkerMap();
        if (mode == 1){
            //remove the leader from the list if still there
            if (stalkerMap.containsKey(cfg.getLeader_id())){
                stalkerMap.remove(cfg.getLeader_id());
            }
            //call election as usual
            Debugger.log("Election: Re-election has started...", null);
            //reelect without the old leader
        }
        else if  (mode == 0){
            //regular election initial conditions
            Debugger.log("Election: Voting has started...", null);
        }

        // ask for leader
        Map<Integer, Integer> voteCount = new HashMap<>();
        for(Integer entry : stalkerMap.keySet()) {
            voteCount.put(entry, askForLeader(entry));
        }
        int max = Integer.MIN_VALUE;
        for(Integer i: voteCount.keySet())
        {
            if(voteCount.get(i) > max)
            {
                max = voteCount.get(i);
                leaderUuid = i;
                if(!voteCount.containsKey(leaderUuid))
                {
                    voteCount.put(leaderUuid,1);
                }else
                {
                    int newCount = voteCount.get(leaderUuid) + 1;
                    voteCount.put(leaderUuid,newCount);
                }
            }
        }
        ConfigManager.getCurrent().setLeader_id(leaderUuid);
        Debugger.log("Election: Leader selected: " + leaderUuid, null);
    }


    public boolean tryLeader(){

        stalkerMap = updateStalkerMap();
        Debugger.log("Trying to find a running leader", null);
        //try and connect to a leader
        for(Map.Entry<Integer, String> entry : stalkerMap.entrySet())
        {
            int port = cfg.getLeader_report();
            Socket socket;
            try {
                socket = NetworkUtils.createConnection(entry.getValue(), cfg.getLeader_report());
                socket.setSoTimeout(200);
                // create a leader packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                if (commsHandler.sendPacket(socket, MessageType.DISCOVER, "", true) == MessageType.ACK){
                    cfg.setLeader_id(entry.getKey());
                    ConfigManager.saveToFile(cfg);
                    Debugger.log("Leader found", null);
                    try {
                        socket.close();
                    }
                    catch (Exception e){

                    }
                    return true;
                }
            }catch (Exception e) {
                //Debugger.log("", e);
            }
        }
        Debugger.log("No leader found", null);
        return false;
    }

    public int askForLeader(int entry){
        stalkerMap = updateStalkerMap();
        int port = cfg.getLeader_report();
        int timeoutForReply = 4;
        Socket socket = null;
        try {
            socket = NetworkUtils.createConnection(stalkerMap.get(entry), port);
            if (socket != null){
                //socket.setSoTimeout(1000 * timeoutForReply);

                // create a leader packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                MessageType m = null;
                //if messagetype is ack then this is a new
                m = commsHandler.sendPacket(socket, MessageType.LEADER, "Asking for a Leader", true);
                if (m == MessageType.ACK){
                    // listen for other people leader
                    TcpPacket tcpPacket = commsHandler.receivePacket(socket);
                    String  content = tcpPacket.getMessage();
                    //get the result of the vote
                    ObjectMapper mapper = new ObjectMapper();
                    Optional<ElectionPacket> ep = null;
                    ep = Optional.of(mapper.readValue(content, ElectionPacket.class));

                    leaderUuid = Integer.valueOf(ep.get().getUuid());
                    leaderIP = ep.get().getIp();
                    //System.out.println("Election vote: " + leaderUuid + ", " + leaderIP);
                }
            }


        } catch (SocketException e) {
            // ask another stalker for the leader if fails to establish connection with one of the stalker
            Debugger.log("", e);
        } catch (IOException e) {
            // ask another stalker for the leader if fails to establish connection with one of the stalker
            Debugger.log("", e);
        }catch(Exception e) {
        }
        closeSocket(socket);
        return(leaderUuid);

    }

    public void closeSocket(Socket s){
        try{
            if(s != null) {
                s.close();
            }
        } catch (Exception e) {
            Debugger.log("", e);
        }
    }

    public static HashMap<Integer, String> updateStalkerMap() {
        return NetworkUtils.getStalkerMap(cfg.getStalker_list_path());
    }

    /** Getters and Setters*/
    public static int getLeaderUuid() {
        return leaderUuid;
    }

    public static void setLeaderUuid(int leaderUuid) {
        LeaderCheck.leaderUuid = leaderUuid;
    }

    public static String getLeaderIP() {
        return leaderIP;
    }

    public static void setLeaderIP(String leaderIP) {
        LeaderCheck.leaderIP = leaderIP;
    }


}
