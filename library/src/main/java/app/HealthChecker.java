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
    private boolean debugMode;
    private  ConfigFile cfg;


    /**
     * This constructor is meant to be used from JCP and STALKER to do health checks on STALKERS
     * @param  checker health check request sender module
     * @param spaceAvailableSoFar stalker will have this null
     */
    public HealthChecker(Module checker, AtomicLong spaceAvailableSoFar, boolean debugMode){
        requestSender = checker;
        HashMap<Integer, String> stalkers =  NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getStalker_list_path()));
        stalkerList = stalkers;

        if(checker == Module.STALKER){
            HashMap<Integer, String> harms =  NetworkUtils.mapFromJson(NetworkUtils.fileToString(ConfigManager.getCurrent().getHarm_list_path()));
            harmList = harms;
        }
        this.spaceAvailableSoFar = spaceAvailableSoFar;
        this.debugMode = debugMode;
    }


    /**
     * This method schedules TimerTask for each node in the config file
     * TimerTask is a runnable that executes run method at fixed interval
     */
    @Override
    public void run() {

        Timer timer = new Timer();
        cfg = ConfigManager.getCurrent();
        // for each node in the stalker list, scheduling a task to occur at interval
        if(stalkerList != null) {
            for (Map.Entry<Integer, String> entry : stalkerList.entrySet()) {
                Debugger.log("Health Checker: Starting scheduled health task for stalker node: " + entry.getValue(), null);
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


        /***
         * This block is going to check changes in the list every interval and if there is any new entry
         * it will start a timer Task for it
         */
        while (!Thread.interrupted()){
            try {
                Map<Integer, String> newStalkers =  NetworkUtils.mapFromJson(NetworkUtils
                        .fileToString(cfg.getStalker_list_path()));

                //checking if new stalker list contains any new node
                if(!this.stalkerList.equals(newStalkers)){
                    for(Map.Entry<Integer, String> entry : newStalkers.entrySet()){
                        if(!this.stalkerList.containsKey(entry.getKey())){
                            if(debugMode) {
                                Debugger.log("Health Checker: New node detected" + entry.getValue(), null);
                            }
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
                //checking if harm list contains any new harm node
                if(this.requestSender == Module.STALKER){
                    Map<Integer, String> newHarms =  NetworkUtils.mapFromJson(NetworkUtils
                            .fileToString(cfg.getHarm_list_path()));

                    if(!this.harmList.equals(newHarms)) {
                        ObjectMapper mapper = new ObjectMapper();
                        for (Map.Entry<Integer, String> entry : newHarms.entrySet()) {
                            if(!this.harmList.containsKey(entry.getKey())) {
                                addTimerTaskForHarm(timer, mapper, entry);
                            }
                        }
                        this.harmList = new HashMap<>();
                        this.harmList.putAll(newHarms);
                    }
                }


            } catch (Exception e) {
                Debugger.log("", e);
            }
            try{Thread.sleep(interval);}catch (Exception e){};
        }

    }

    /**
     * Creates a Timer task for Harm
     * @param timer
     * @param mapper
     * @param entry
     */
    private void addTimerTaskForHarm(Timer timer, ObjectMapper mapper, Map.Entry<Integer, String> entry) {
        NodeAttribute attributes = null;
        try {
            attributes = mapper.readValue(entry.getValue(), NodeAttribute.class);
        } catch (IOException e) {
            Debugger.log("", e);
        }
        Debugger.log("Health Checker: Starting scheduled health task for harm node: " + attributes.getAddress(), null);
        addTimerTask(timer, entry.getKey(),
                attributes.getAddress(),
                null,
                Module.HARM);
    }


    /**
     * creates timer task for stalker node
     * @param timer
     * @param uuid
     * @param host
     * @param spaceAvailableSoFar
     * @param stalker
     */
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
     * This is the actual runnable task that will execute run method at the given interval to
     * health check each node
     */
    class HealthCheckerTask extends TimerTask {

        private final String host ;
        private final int uuid;
        private final int port = ConfigManager.getCurrent().getElection_port();

        //will wait 30 seconds for reply, if not then it will be considered dead
        private final int timeoutForReply = 3000;

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
            if(debugMode) {
                Debugger.log("Health Checker Task for host: " + host +
                        " started at: " + NetworkUtils.timeStamp(1), null);
            }
            Socket socket = null;
            try {

                socket = NetworkUtils.createConnection(host, port);
                if (socket != null){
                    //if server does not reply within specified timeout, then SocketException will be thrown
                    socket.setSoTimeout(timeoutForReply);
                    CommsHandler commsHandler = new CommsHandler();
                    //sending the health check request
                    commsHandler.sendPacketWithoutAck(socket, MessageType.HEALTH_CHECK, "REQUEST");
                    //receive packet from node
                    TcpPacket tcpPacket = commsHandler.receivePacket(socket);
                    if (tcpPacket != null){
                        String  content = tcpPacket.getMessage();
                        ObjectMapper mapper = new ObjectMapper();

                        // parse the packet content
                        JsonNode healthCheckReply = mapper.readTree(content);
                        Module sender = Module.valueOf(healthCheckReply.get("sender").asText());
                        String status = healthCheckReply.get("status").textValue();
                        long availableSpace =  healthCheckReply.get("diskSpace").asLong();
                       // Map<String, String> corruptedList = healthCheckReply.get("corruptedChunks").


                        if(status.equals("SUCCESS")){
                            if(target == Module.STALKER && this.spaceToUpdate != null) {
                                this.spaceToUpdate.set(availableSpace);
                                if(debugMode) {
                                    Debugger.log("Health Checker: Status was success for health check and disk space available "
                                            + this.spaceToUpdate.get(), null);
                                }
                            }else if(target == Module.HARM){
                                // need to add the space for all HARMS in config file
                                NetworkUtils.updateHarmList(String.valueOf(this.uuid),
                                        availableSpace,
                                        true);
                            }
                        }else if(status.equals("CORRUPT") && sender == Module.HARM){

                            // do something with the corrupted chunks

                        }
                    }

                }


            }
            catch(NullPointerException e){
            }
            catch (SocketException e) {
                // server has not replied within expected timeoutTime
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", e);
                }
            } catch (IOException e) {
                // any other IO exception, also stop the task and assume the node is dead
                Debugger.log("STALKER at : " + host  + "has died!", null);
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", e);
                }
            }finally {
                try{
                    if(socket != null) {
                        if(debugMode) {
//                            Debugger.log("Health Checker: Task completed closing Socket"
//                                    + this.spaceToUpdate.get(), null);
                        }
                        socket.close();
                    }
                } catch (Exception e) {
                    Debugger.log("", e);
                }
            }
        }


        private void updateConfigAndEndTask(){
            if(this.target == Module.STALKER) {
                Debugger.log("Debug: updataeconfigandeexit 1", null);
                // remove node from STALKER LIST in config file stalkers.list
                NetworkUtils.deleteNodeFromConfig(cfg.getStalker_list_path(), String.valueOf(this.uuid));
                int leaderuuid = cfg.getLeader_id();
                // Identify which STALKER went down
                HashMap<Integer, String> stalkerMap = NetworkUtils.getStalkerMap(cfg.getStalker_list_path());
                List<Integer> ids  = NetworkUtils.getStalkerList(cfg.getStalker_list_path());

                stalkerMap.keySet().contains(LeaderCheck.getLeaderUuid());
                if(uuid == cfg.getLeader_id())
                {
                    Debugger.log("Leader has died", null);
                    //send update signal


//                    // kill and the threads
//                    for(Map.Entry<Integer, String> entry : stalkerMap.entrySet())
//                    {
//                        int port = cfg.getLeader_report();
//                        Socket socket;
//                        try {
//                            socket = NetworkUtils.createConnection(entry.getValue(), port);
//                            // create a leader packet and send it to this host
//                            CommsHandler commsHandler = new CommsHandler();
//                            commsHandler.sendPacketWithoutAck(socket, MessageType.KILL, "KILL");
//
//                        }catch (IOException e) {
//                            Debugger.log("", e);
//                        }
//                    }
                }
            }else {
                // don't remove but mark as dead in HARM list
                NetworkUtils.updateHarmList(String.valueOf(this.uuid), -1, false );
            }

            //cancel task
            Debugger.log("Health Checker: Error Occurred Cancelling scheduled task for " + this.host, null);
            cancel();
        }
    }
}
