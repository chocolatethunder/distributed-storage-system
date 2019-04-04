package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *This class is responsible for scheduling task for doing health checks on all units in the config files( HARM or STALKERS)
 */
public class HealthChecker implements Runnable{

    // every 30 seconds for now
    private final long interval = 1000 * 30;

    //delay between each timerTask, considering net discovery already occured
    private final long intialDelay = 1000 * 20;

    private Map<Integer, String> stalkerList;
    private Map<Integer, String> harmList;
    private Module requestSender;
    private AtomicLong spaceAvailableSoFar;


    /**
     * This constructor is meant to be used from JCP and STALKER to do health checks on STALKERS
     * @param  checker health check request sender module
     * @param spaceAvailableSoFar stalker will have this null
     */
    public HealthChecker(Module checker, AtomicLong spaceAvailableSoFar){
        requestSender = checker;
        HashMap<Integer, String> stalkers =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/stalkers.list"));
        stalkerList = stalkers;

        if(checker == Module.STALKER){
            HashMap<Integer, String> harms =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/harm.list"));
            harmList = harms;
        }
        this.spaceAvailableSoFar = spaceAvailableSoFar;
    }


    /**
     * This method schedules TimerTask for each node in the config file
     * TimerTask is a runnable that executes run method at fixed interval
     */
    @Override
    public void run() {

        Timer timer = new Timer();


        // for each node in the stalker list, scheduling a task to occur at interval
        if(stalkerList != null) {
            for (Map.Entry<Integer, String> entry : stalkerList.entrySet()) {
                System.out.println("Starting scheduled health task for stalker node: " + entry.getValue());
                addTimerTask(timer,
                        entry.getKey(),
                        entry.getValue(),
                        spaceAvailableSoFar,
                        Module.STALKER);
            }
        }

        if(harmList != null) {
            ObjectMapper mapper = new ObjectMapper();
            // for each node in the harm list, scheduling a task to occur at interval
            for (Map.Entry<Integer, String> entry : harmList.entrySet()) {
                addTimerTaskForHarm(timer, mapper, entry);
            }
        }

        while (!Thread.interrupted()){
            try {
                Map<Integer, String> newStalkers =  NetworkUtils.mapFromJson(NetworkUtils
                        .fileToString("config/stalkers.list"));

                if(!this.stalkerList.equals(newStalkers)){
                    for(Map.Entry<Integer, String> entry : newStalkers.entrySet()){
                        if(!this.stalkerList.containsKey(entry.getKey())){
                            addTimerTask(timer,
                                    entry.getKey(),
                                    entry.getValue(),
                                    spaceAvailableSoFar,
                                    Module.STALKER);
                        }
                    }

                    this.stalkerList = new HashMap<>();
                    this.stalkerList.putAll(newStalkers);
                }

                if(this.requestSender == Module.STALKER){
                    Map<Integer, String> newHarms =  NetworkUtils.mapFromJson(NetworkUtils
                            .fileToString("config/harm.list"));

                    if(!this.harmList.equals(newHarms)) {
                        ObjectMapper mapper = new ObjectMapper();
                        for (Map.Entry<Integer, String> entry : newHarms.entrySet()) {
                            addTimerTaskForHarm(timer, mapper, entry);
                        }
                        this.harmList = new HashMap<>();
                        this.harmList.putAll(newHarms);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            try{Thread.sleep(interval * 1000);}catch (Exception e){};
        }



    }

    private void addTimerTaskForHarm(Timer timer, ObjectMapper mapper, Map.Entry<Integer, String> entry) {
        NodeAttribute attributes = null;
        try {
            attributes = mapper.readValue(entry.getValue(), NodeAttribute.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting scheduled health task for harm node: " + attributes.getAddress());
        addTimerTask(timer, entry.getKey(),
                attributes.getAddress(),
                null,
                Module.HARM);
    }



    private void addTimerTask(Timer timer,
                              Integer uuid,
                              String host,
                              AtomicLong spaceAvailableSoFar,
                              Module stalker) {
        timer.scheduleAtFixedRate(new HealthCheckerTask(uuid,
                        host,
                        spaceAvailableSoFar,
                        stalker),
                intialDelay,
                interval);
    }


    /**
     * This is the actual runnable task that will execute run method at the given interval
     */
    class HealthCheckerTask extends TimerTask {

        private final String host ;
        private final int uuid;
        private final int port = 11114;

        //will wait 30 seconds for reply, if not then it will be considered dead
        private final int timeoutForReply = 1000 * 30;

        private AtomicLong spaceToUpdate;
        private Module target;

        HealthCheckerTask( int uuid, String host, AtomicLong  spaceToUpdate, Module type){
            this.uuid = uuid;
            this.host = host;
            this.spaceToUpdate = spaceToUpdate;
            this.target = type;

        }

        @Override
        public void run() {
            System.out.println("Health Checker Task for host: "+ host + " started at: " + NetworkUtils.timeStamp(1));
            Socket socket = null;
            try {

                socket = NetworkUtils.createConnection(host, port);

                //if server does not reply within specified timeout, then SocketException will be thrown
                socket.setSoTimeout(1000 * timeoutForReply);


                CommsHandler commsHandler = new CommsHandler();
                //sending the health check request
                commsHandler.sendPacketWithoutAck(socket, MessageType.HEALTH_CHECK, "REQUEST");


                //receive packet from node
                TcpPacket tcpPacket = commsHandler.receivePacket(socket);

                String  content = tcpPacket.getMessage();

                ObjectMapper mapper = new ObjectMapper();

                // parse the packet content
                JsonNode healthCheckReply = mapper.readTree(content);
                Module sender = Module.valueOf(healthCheckReply.get("sender").asText());
                String status = healthCheckReply.get("status").textValue();
                long availableSpace =  healthCheckReply.get("diskSpace").asLong();


                if(status.equals("SUCCESS")){
                    if(target == Module.STALKER && this.spaceToUpdate != null) {
                        this.spaceToUpdate.set(availableSpace);
                        System.out.println("Status was success for health check and disk space available "
                                + this.spaceToUpdate.get());
                    }else if(target == Module.HARM){
                        // need to add the space for all HARMS in config file
                        NetworkUtils.updateHarmList(String.valueOf(this.uuid),
                                availableSpace,
                                true);
                    }
                }

                if(status.equals("CORRUPT") && sender == Module.HARM){
                    // deal with corrupt chunks here

                }





            } catch (SocketException e) {
                // server has not replied within expected timeoutTime
                updateConfigAndEndTask();
                e.printStackTrace();
            } catch (IOException e) {

                // any other IO exception, also stop the task and assume the node is dead
                updateConfigAndEndTask();
                e.printStackTrace();
            }finally {
                try{
                    if(socket != null) {
                        System.out.println("Task completed closing Socket");
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private void updateConfigAndEndTask(){
            if(this.target == Module.STALKER) {
                // remove node from STALKER LIST in config file stalkers.list
                NetworkUtils.deleteNodeFromConfig("config/stalkers.list", String.valueOf(this.uuid));
            }else{
                // don't remove but mark as dead in HARM list
                NetworkUtils.updateHarmList(String.valueOf(this.uuid), -1, false );
            }

            //cancel task
            System.out.println("Cancelling scheduled task for " + this.host);
            cancel();
        }
    }
}
