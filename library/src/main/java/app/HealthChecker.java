package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *This class is responsible for scheduling task for doing health checks on all units in the config files( HARM or STALKERS)
 */
public class HealthChecker implements Runnable{


    // every 30 seconds for now
    private long interval = 1000 * 2;

    //delay between each timerTask, considering net discovery already occured
    private long intialDelay = 1000 *10;

    private Map<Integer, String> stalkerList;
    private Map<Integer, String> harmList;
    private Module requestSender;
    private AtomicLong spaceAvailableSoFar;
    private boolean debugMode;
    private  ConfigFile cfg;
    private boolean interrupted = false;


    /**
     * This constructor is meant to be used from JCP and STALKER to do health checks on STALKERS
     * @param  checker health check request sender module
     * @param spaceAvailableSoFar stalker will have this null
     */
    public HealthChecker(Module checker, AtomicLong spaceAvailableSoFar, boolean debugMode){
        cfg = ConfigManager.getCurrent();
        requestSender = checker;
        stalkerList =  NetworkUtils.getStalkerMap(cfg.getStalker_list_path());

        if(checker == Module.STALKER){
            harmList =  NetworkUtils.mapFromJson(NetworkUtils.fileToString(cfg.getHarm_list_path()));
        }
        this.spaceAvailableSoFar = spaceAvailableSoFar;
        this.debugMode = debugMode;
        //interval = cfg.getStalker_update_freq() * 1100;

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
               // Debugger.log("Health Checker: Starting scheduled health task for stalker node: " + entry.getValue(), null);
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
        while (!interrupted){
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
                        if (interrupted){
                            break;
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
                            if (interrupted){
                                break;
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
        Debugger.log("Health checker suspended...", null);

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
        //Debugger.log("Health Checker: Starting scheduled health task for harm node: " + attributes.getAddress(), null);
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
        private int port;

        //will wait 30 seconds for reply, if not then it will be considered dead
        private final int timeoutForReply = 10000;

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
            port = ConfigManager.getCurrent().getHealth_check_port();
            if(debugMode) {
                //Debugger.log("Health Checker Task for host: " + host +
                       // " started at: " + NetworkUtils.timeStamp(1), null);
            }
            Socket socket = null;
            try {

                socket = NetworkUtils.createConnection(host, port);
                if (socket != null){
                    //if server does not reply within specified timeout, then SocketException will be thrown
                    //socket.setSoTimeout(timeoutForReply);
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
                        }

                        if(status.equals("CORRUPT") && sender == Module.HARM){
                            // deal with corrupt chunks here

                        }
                    }
                }
            }
            catch(NullPointerException e){
            }
            catch (SocketException e) {
                //(socket);
                // server has not replied within expected timeoutTime
                Debugger.log("Module at : " + host  + "has died!", null);
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", e);
                }

            } catch (IOException e) {
                // any other IO exception, also stop the task and assume the node is dead
                Debugger.log("Module at : " + host  + "has died!", null);
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", e);
                }
            }
            finally {

            }
            closeSocket(socket);
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
        private void updateConfigAndEndTask(){
            if(this.target == Module.STALKER) {
                // remove node from STALKER LIST in config file stalkers.list
                HashMap<Integer, String> stalkerMap = NetworkUtils.getStalkerMap(cfg.getStalker_list_path());
                NetworkUtils.deleteNodeFromConfig(cfg.getStalker_list_path(), String.valueOf(this.uuid));
                // Identify which STALKER went down

//                stalkerMap.keySet().contains(LeaderCheck.getLeaderUuid());
                if(uuid == cfg.getLeader_id())
                {
                    if(stalkerMap.containsKey(uuid)){
                        stalkerMap.remove(uuid);
                    }
                    Debugger.log("Leader has died", null);
                    //send update signal
                    // kill and the threads
                    for(Map.Entry<Integer, String> entry : stalkerMap.entrySet())
                    {
                        int port = 11115;
                        Socket socket;
                        try {
                            socket = NetworkUtils.createConnection(entry.getValue(), port);
                            if (socket != null){
                                // create a leader packet and send it to this host
                                CommsHandler commsHandler = new CommsHandler();
                                commsHandler.sendPacketWithoutAck(socket, MessageType.REELECT, "");
                                //socket.close();
                            }
                        }catch (IOException e) {
                            Debugger.log("", e);
                        }
                    }
                    interrupted = true;
                }
                else{
                    Debugger.log("A worker has died", null);
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
