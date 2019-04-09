package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class LeaderCheck {

    private static HashMap<Integer, String> stalkerMap;   // stalker list
    private static List<Integer> ids = new ArrayList<>();                   // stalker uuids

    private static int leaderUuid = -1;
    private static String leaderIP = "x.x.x.x";
    private static ConfigFile cfg;
    public LeaderCheck()
    {
        this.stalkerMap = NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getStalker_list_path()));
        this.ids = NetworkUtils.mapToSList(stalkerMap);

    }



    public static void election()
    {
        Map<Integer, Integer> voteCount = new HashMap<>();
        cfg = ConfigManager.getCurrent();
        // ask for leader
        Debugger.log("Election: Voting has started...", null);
        for(Integer entry : stalkerMap.keySet())
        {
            int port = cfg.getElection_port();
            int timeoutForReply = 20;


            Socket socket = null;
            try {
                socket = NetworkUtils.createConnection(stalkerMap.get(entry), port);
                socket.setSoTimeout(1000 * timeoutForReply);

                // create a leader packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                if (commsHandler.sendPacket(socket, MessageType.LEADER, "Asking for a Leader", true) == MessageType.ACK){
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
                    if(!voteCount.containsKey(leaderUuid))
                    {
                        voteCount.put(leaderUuid,1);
                    }else
                    {
                        int newCount = voteCount.get(leaderUuid) + 1;
                        voteCount.put(leaderUuid,newCount);

                    }
                }



            } catch (SocketException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                Debugger.log("", e);
            } catch (IOException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                Debugger.log("", e);
            }finally {
                try{
                    socket.close();
                } catch (IOException e) {
                    Debugger.log("", e);
                }
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
        Debugger.log("Election: Leader selected: " + leaderUuid, null);
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
