package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderCheck implements Runnable {

    private static HashMap<Integer, String> stalkerMap;   // stalker list
    private static List<Integer> ids = new ArrayList<>();                   // stalker uuids

    private static int leaderUuid = -1;
    private static String leaderIP = "x.x.x.x";

    public LeaderCheck(HashMap<Integer, String> stalkerMap, List<Integer> ids)
    {
        this.stalkerMap = stalkerMap;
        this.ids = ids;
    }


    @Override
    public void run() {

        askForLeader();

    }

    public static void askForLeader()
    {
        // ask for leader
        for(Map.Entry<Integer, String> entry : stalkerMap.entrySet()) {
            int port = 11114;
            int timeoutForReply = 5;

            System.out.println("Asking for a leader");
            Socket socket = null;
            try {
                socket = NetworkUtils.createConnection(entry.getValue(), port);
                socket.setSoTimeout(1000 * timeoutForReply);

                // create a leader packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                commsHandler.sendPacketWithoutAck(socket, MessageType.LEADER, "Asking for a Leader");

                // listen for other people leader
                TcpPacket tcpPacket = commsHandler.receivePacket(socket);
                String  content = tcpPacket.getMessage();

                // parse the content to to get the leader uuid
                ObjectMapper mapper = new ObjectMapper();
                JsonNode leaderReply = mapper.readTree(content);
                leaderUuid = Integer.valueOf(leaderReply.get("uuid").asText());
                leaderIP = leaderReply.get("ip").textValue();

            } catch (SocketException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                e.printStackTrace();
            } catch (IOException e) {
                // ask another stalker for the leader if fails to establish connection with one of the stalker
                e.printStackTrace();
            }finally {
                try{
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
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
