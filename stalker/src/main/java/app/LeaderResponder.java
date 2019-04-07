package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  Responds to other people leader ship election request and update its the leader
 *
 * */

public class LeaderResponder implements Runnable {

    private Socket socket;
    private static HashMap<Integer, String> stalkerMap;   // stalker list
    private static List<Integer> ids;                   // stalker uuids

    public LeaderResponder (Socket socket)
    {
        this.socket = socket;
        String stalkerList = NetworkUtils.fileToString("config/stalkers.list");
        this.stalkerMap = NetworkUtils.mapFromJson(stalkerList);
        ids = NetworkUtils.mapToSList(stalkerMap);
    }

    public void sendLeader()
    {
        // create a election packet and send it to this host
        CommsHandler commsHandler = new CommsHandler();
        commsHandler.sendResponse(this.socket, MessageType.ACK);
        commsHandler.sendPacketWithoutAck(this.socket, MessageType.ELECTION, electionPacket());

        try{
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Response Packet not using rn because assuming everyone knows everyone
    public static String electionPacket()
    {
        // entry.getValue(); // get the IP of the this UUID
        // entry.getKey();   // uuid;
        int leader = ids.get(0);
        ElectionPacket elecPacket = new ElectionPacket(String.valueOf(leader), String.valueOf(stalkerMap.get(leader)));
        String electionPkt = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Sending out broadcast with signature: " + mapper.writeValueAsString(elecPacket) + "\n");
            electionPkt = mapper.writeValueAsString(elecPacket);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return electionPkt;

    }

    /** May be used later*/
    public static void broadcastLeader(){

        // broadcast this leader
        for(Map.Entry<Integer, String> entry : stalkerMap.entrySet()) {
            // entry.getValue(); // get the IP of the this UUID
            // entry.getKey();   // uuid;

            int port = 11114;
            int timeoutForReply = 5;

            System.out.println("Election In Progress");
            Socket socket = null;
            try {
                socket = NetworkUtils.createConnection(entry.getValue(), port);
                socket.setSoTimeout(1000 * timeoutForReply);

                //sending the health check request
                // create a election packet and send it to this host
                CommsHandler commsHandler = new CommsHandler();
                String electionPacket = electionPacket();
                commsHandler.sendPacketWithoutAck(socket, MessageType.ELECTION, electionPacket);

                // listen for other people leader

            } catch (SocketException e) {

                // server has not replied within expected timeoutTime
                e.printStackTrace();
            } catch (IOException e) {
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

    // reply with the leader info
    @Override
    public void run() {
        sendLeader();
    }
}
