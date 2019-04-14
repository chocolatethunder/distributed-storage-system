package app.LeaderUtils;

import app.*;
import app.chunk_util.Indexer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
            Debugger.log("Trying: " + entry + " " + stalkerMap.get(entry), null);
            try{

                int vote = askForLeader(entry);

                if (voteCount.containsKey(vote)){
                    voteCount.put(vote, voteCount.get(vote) + 1);
                }
                else{
                    voteCount.put(vote, 1);
                }

            }
            catch (NullPointerException e){
                Debugger.log("RUOH", null);
            }

        }
        int max = Integer.MIN_VALUE;
        for(Integer i: voteCount.keySet())
        {
            if(voteCount.get(i) > max)
            {
                max = voteCount.get(i);
                leaderUuid = i;
            }
        }
        ConfigManager.getCurrent().setLeader_id(leaderUuid);
        Debugger.log("Election: Leader selected: " + leaderUuid + "with " + max + " votes!", null);
    }

    public boolean tryLeader(){
        stalkerMap = updateStalkerMap();
        Debugger.log("Trying to find a running leader", null);
        //try and connect to a leader
        for(Map.Entry<Integer, String> entry : stalkerMap.entrySet())
        {
            int port = cfg.getLeader_report();
            Socket socket = null;
            try {
                socket = NetworkUtils.createConnection(entry.getValue(), cfg.getLeader_report());
                socket.setSoTimeout(200);
                // create a leader packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                if (commsHandler.sendPacket(socket, MessageType.DISCOVER, "", true) == MessageType.ACK){
                    commsHandler.sendPacket(socket, MessageType.ACK, "", false);
                    TcpPacket t = commsHandler.receivePacket(socket);
                    if (t.getMessageType() == MessageType.ACK){
                       // TcpPacket t = commsHandler.receivePacket(socket);
                        //System.out.println("HOOOOOOOOOOOOO:     \n\n" + t.getMessage());
                        cfg.setLeader_id(entry.getKey());
                        ConfigManager.saveToFile(cfg);
                        Indexer.saveToFile(Indexer.fromString(t.getMessage()));
                        Debugger.log("Leader found", null);
                        NetworkUtils.closeSocket(socket);
                        return true;
                    }
                }

            }catch (Exception e) {
                //Debugger.log("", e);
            }
            finally {
                NetworkUtils.closeSocket(socket);
            }
        }

        Debugger.log("No leader found", null);
        return false;
    }

    public int askForLeader(int entry){
        stalkerMap = updateStalkerMap();
        int port = cfg.getElection_port();
        int timeoutForReply = 4;
        Socket socket = null;
        int attempts = 0;


        while (attempts < 4){
            try {
                Debugger.log("Leadercheck: Asking for leader from: " + entry + "at " + stalkerMap.get(entry) + "on port: " + port, null);
                socket = NetworkUtils.createConnection(stalkerMap.get(entry), port);
                socket.setSoTimeout(1000);
                if (socket != null){
                    //socket.setSoTimeout(1000 * timeoutForReply);
                    Debugger.log("Leadercheck: Connection established", null);
                    // create a leader packet and send it to this host
                    CommsHandler commsHandler = new CommsHandler();
                    MessageType m = null;
                    //if messagetype is ack then this is a new
                    m = commsHandler.sendPacket(socket, MessageType.LEADER, "Asking for a Leader", true);
                    //Debugger.log("Message from ack: " + m.toString() , null);

                    if (m == MessageType.ACK){
                        Debugger.log("Ack recieved from: " + stalkerMap.get(entry), null);
                        // listen for other people leader
                        TcpPacket tcpPacket = commsHandler.receivePacket(socket);
                        String  content = tcpPacket.getMessage();
                        //get the result of the vote
                        ObjectMapper mapper = new ObjectMapper();
                        Optional<ElectionPacket> ep = null;
                        ep = Optional.of(mapper.readValue(content, ElectionPacket.class));

                        leaderUuid = Integer.valueOf(ep.get().getUuid());
                        leaderIP = ep.get().getIp();
                        Debugger.log("Leadercheck: Vote recieved " + leaderUuid, null);
                        break;
                        //System.out.println("Election vote: " + leaderUuid + ", " + leaderIP);
                    }
                    Debugger.log("Leadercheck: connection to : " + stalkerMap.get(entry) + " closed", null);
                    NetworkUtils.closeSocket(socket);

                }
            } catch (SocketException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                Debugger.log("", e);
            } catch (IOException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                Debugger.log("", e);
            }catch(Exception e) {
                Debugger.log("", e);
            }
            NetworkUtils.closeSocket(socket);

            NetworkUtils.wait(1000);
            attempts++;
        }



        return(leaderUuid);

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
