package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;

import java.util.HashMap;
import java.util.List;
import app.ConfigManager;


/**
 *
 *  Responds to other people leader ship election request and update its the leader
 *
 * */

public class LeaderResponder implements Runnable {

    private static boolean verbose = false;
    private Socket socket;
    private static HashMap<Integer, String> stalkerMap;   // stalker list
    private static List<Integer> ids;                   // stalker uuids

    public LeaderResponder (Socket socket)
    {
        this.socket = socket;
        Debugger.log("Request for leader vote accepted", null);
    }

    public void sendLeader()
    {
        this.stalkerMap = NetworkUtils.getStalkerMap(ConfigManager.getCurrent().getStalker_list_path());
        ids = NetworkUtils.mapToSList(stalkerMap);
        // create a election packet and send it to this host
        CommsHandler commsHandler = new CommsHandler();
        commsHandler.sendResponse(this.socket, MessageType.ACK);
        commsHandler.sendPacketWithoutAck(this.socket, MessageType.ELECTION, electionPacket());
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
            if (verbose){ Debugger.log("LeaderResponder: Sending out broadcast with signature: " + mapper.writeValueAsString(elecPacket), null);}
            electionPkt = mapper.writeValueAsString(elecPacket);
        } catch (JsonProcessingException e) {
            Debugger.log("", e);
        }
        return electionPkt;
    }


    // reply with the leader info
    @Override
    public void run() {
        sendLeader();
    }
}
