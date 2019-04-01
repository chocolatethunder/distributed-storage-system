package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Tells other stalker about who it thinks the leader is!
 *
 * */

public class ElectionResponder implements Runnable{

    private static HashMap<Integer, String> stalkerMap = new HashMap<>();   // stalker list
    private static List<Integer> ids = new ArrayList<>();                   // stalker uuids
    private Socket socket;
    private String leaderMsg;

    public ElectionResponder(Socket socket, String leaderMsg)
    {
        this.socket = socket;
        this.leaderMsg = leaderMsg;
    }


    @Override
    public void run() {

        System.out.println("Parse leader packet!");
        String pktLeaderIp = getPktLeaderIp(leaderMsg);

        int pktLeaderId = Integer.valueOf(getPktLeaderId(leaderMsg));
        int localLeader = ids.get(0);
        int leader = -1;
        // compare global and local leader
        if(localLeader >= pktLeaderId)
        {
            leader = pktLeaderId;
        } else {
            leader = localLeader;
        }

        System.out.println("Election responder");
        CommsHandler commsHandler = new CommsHandler();
        commsHandler.sendPacketWithoutAck(this.socket, MessageType.ELECTION, electionPakcet() );

    }

    public String electionPakcet()
    {
        int localLeader = ids.get(0);
        ElectionPacket elecPacket = new ElectionPacket(String.valueOf(localLeader), stalkerMap.get(localLeader));
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

    public String getPktLeaderId(String leaderMsg)
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode request;
        String req_leaderUuid = null;
        try {
            request = mapper.readTree(leaderMsg);
            req_leaderUuid = request.get("leaderUuid").textValue();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return req_leaderUuid;
    }

    public String getPktLeaderIp(String leaderMsg)
    {
        // parse the packet
        System.out.println("A discovery probe was received: " + leaderMsg);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode request;
        String req_leaderIp = null;
        try {
            request = mapper.readTree(leaderMsg);
            req_leaderIp = request.get("leaderIp").textValue();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return req_leaderIp;
    }

}
