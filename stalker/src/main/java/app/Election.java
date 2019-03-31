package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Election implements Runnable {

    // updates the role in the app.java

    // HARDCODING A LIST HERE
    // Map of uuids and string representaion of IP Address
    private static HashMap<Integer, String> stalkerMap = new HashMap<>();
    // list of the UUIDS
    private static List<Integer> ids = new ArrayList<>();
    private static int role = -1;


    public Election()
    {
        initStalkerMap();

    }


    public void start(HashMap<Integer, String> stalkers) {

        // for each node in the list, scheduling a task to occur at interval
        for(Map.Entry<Integer, String> entry : stalkers.entrySet()) {
            System.out.println("Starting scheduled health task for node: " + entry.getValue());
        }


    }

    public void initStalkerMap() {
        for(int i = 0; i < 5; i++)
        {
            ids.add(i);
            stalkerMap.put(i,"x.x.x.x");
        }
    }

    public static int identifyRole()
    {
        // if your id is the same as the leader ids
        if(NetworkUtils.getMacID() == ids.get(0))
        {
            return 0;
        }
        // if you are the vice-leader
        else if (NetworkUtils.getMacID() == ids.get(1))
        {
            return 2;
        }
        // if you are normal request handling stalker
        else
        {
            return 1;
        }

    }

    public static int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }


    public void holdElection()
    {
        //wait for the initial election to be over
        Thread electionThread = new Thread(new Election());
        electionThread.start();
        try {
            electionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        // read the confiq file

        // get the local leader
        int localLeader = ids.get(0);

        // broadcast this leader
        for(Map.Entry<Integer, String> entry : stalkerMap.entrySet()) {
            entry.getValue(); // get the IP of the this UUID
            entry.getKey();   // uuid;


            int port = 1450;
            int timeoutForReply = 5;

            System.out.println("Election In Progress");
            Socket socket = null;
            try {

                socket = NetworkUtils.createConnection(entry.getValue(), port);
                //if server does not reply within 5 seconds, then SocketException will be thrown
                socket.setSoTimeout(1000 * timeoutForReply);

                CommsHandler commsHandler = new CommsHandler();
                //sending the health check request
                // create a election packet and send it to this host
                ElectionPacket elecPacket = new ElectionPacket(String.valueOf(localLeader), entry.getValue());
                ObjectMapper mapper = new ObjectMapper();
                System.out.println("Sending out broadcast with signature: " + mapper.writeValueAsString(elecPacket) + "\n");
                String electionPacket = mapper.writeValueAsString(elecPacket);
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


        // agree on the leader

        // change the role of the Stalker


        // assuming ideal case
        int stalkerRole = identifyRole();
        setRole(stalkerRole);


    }
}

