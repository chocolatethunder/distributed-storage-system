package app;

import app.chunk_util.Chunk;
import app.chunk_util.IndexFile;
import app.chunk_util.Indexer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import app.*;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 *This class is responsible for scheduling task for doing health checks on all units in the config files( HARM or STALKERS)
 */
public class HealthChecker implements Runnable{


    // every 30 seconds for now
    private final long interval = 1000 * 2;

    //delay between each timerTask, considering net discovery already occured
    private final long intialDelay = 1000 * 10;

    private Map<Integer, String> stalkerList;
    private HashMap<Integer, NodeAttribute> harmList;
    private HashMap<Integer, NodeAttribute> harmHistory;
    //private Map<Integer, String> harmList;
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

            //
            //harmList =  NetworkUtils.getNodeMap(cfg.getHarm_list_path());

            //start initially with no harms listed in the system
            harmList = new HashMap<>();

            //this is the history of harms that have been on the system
            //we need to keep a consistent map of these or there will be errors when trying to get files from downed harms
            try{
                harmHistory =  NetworkUtils.getNodeMap(cfg.getHarm_hist_path());
                if (harmHistory == null){
                    throw new IOException("HarmHist corrupted");
                }
            }
            catch (Exception e){
                    Debugger.log("HarmHist corrupted", null);
                    File f = new File(cfg.getHarm_hist_path());
                    if (!f.exists()){
                        try{
                            HashMap<Integer, NodeAttribute> n = new HashMap<>();
                            f.createNewFile();
                            NetworkUtils.toFile(cfg.getHarm_hist_path(), n);
                        }
                        catch (IOException ex){
                        }
                    }
                    else {
                        try{
                            f.delete();
                            HashMap<Integer, NodeAttribute> n = new HashMap<>();
                            f.createNewFile();
                            NetworkUtils.toFile(cfg.getHarm_hist_path(), n);
                        }
                        catch (IOException ex){

                        }
                    }
                harmHistory =  NetworkUtils.getNodeMap(cfg.getHarm_hist_path());
            }


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
            for (Map.Entry<Integer, NodeAttribute> entry : harmList.entrySet()) {
                addTimerTaskForHarm(timer, mapper, entry);
            }
        }


        /***
         * This block is going to check changes in the list every interval and if there is any new entry
         * it will start a timer Task for it
         */
        while (!Thread.currentThread().isInterrupted() && !NetworkUtils.shouldShutDown() && !interrupted){
            try {
                Map<Integer, String> newStalkers =  NetworkUtils.mapFromJson(NetworkUtils
                        .fileToString(cfg.getStalker_list_path()));

                //checking if new stalker list contains any new node
                if(!this.stalkerList.equals(newStalkers)){
                    for(HashMap.Entry<Integer, String> entry : newStalkers.entrySet()){
                        if(!this.stalkerList.containsKey(entry.getKey())){
                            Debugger.log("Health Checker: Stalker at " + entry.getValue() + " is now being tracked.", null);
                            if(debugMode) {
                                Debugger.log("Health Checker: New stalker detected" + entry.getValue(), null);
                            }
                            addTimerTask(timer,
                                    entry.getKey(),
                                    entry.getValue(),
                                    spaceAvailableSoFar,
                                    Module.STALKER);
                        }
                        if (Thread.currentThread().isInterrupted()){
                            break;
                        }
                    }

                    this.stalkerList = new HashMap<>();
                    this.stalkerList.putAll(newStalkers);
                }
                //checking if harm list contains any new harm node
                if(this.requestSender == Module.STALKER && !Thread.currentThread().isInterrupted()){
                    Map<Integer, NodeAttribute> newHarms =  NetworkUtils.getNodeMap(cfg.getHarm_list_path());
                    if(!this.harmList.equals(newHarms)) {

                        ObjectMapper mapper = new ObjectMapper();
                        for (HashMap.Entry<Integer, NodeAttribute> entry : newHarms.entrySet()) {
                            if(!this.harmList.containsKey(entry.getKey())) {
                                addTimerTaskForHarm(timer, mapper, entry);
                                Debugger.log("Health Checker: Harm at " + entry.getValue().getAddress() + " is now being tracked.", null);

                            }
                            //check if we've seen the node before
                            if (!harmHistory.containsKey(entry.getKey())){
                                //put the entry in the list if we haven't seen it before
                                harmHistory.put(entry.getKey(), entry.getValue());
                            }
                            else {
                                //if the address changed we have to update it
                                if (!(harmHistory.get(entry.getKey()).getAddress()).equals(entry.getValue().getAddress())){
                                    harmHistory.get(entry.getKey()).setAddress(entry.getValue().getAddress());
                                }
                                //set as alive
                                harmHistory.get(entry.getKey()).setAlive(true);
                            }

                            if (Thread.currentThread().isInterrupted()){
                                break;
                            }
                        }
                        for (HashMap.Entry<Integer, NodeAttribute> entry : harmList.entrySet()){
                            if(!newHarms.containsKey(entry.getKey())){
                                Debugger.log("Health Checker: Harm at " + entry.getValue().getAddress() + " no longer being tracked.", null);

                            }
                        }

                            if (!Thread.currentThread().isInterrupted()){
                            this.harmList = new HashMap<>();
                            this.harmList.putAll(newHarms);
                            NetworkUtils.toFile(cfg.getHarm_hist_path(), harmHistory);
                        }

                    }
                }


            } catch (Exception e) {
                Debugger.log("", e);
            }
            //Debugger.log("loop", null);
            try{Thread.sleep(interval);}catch (Exception e){};
        }
        Debugger.log("Health checker shutdown init...", null);
        timer.cancel();
        timer.purge();
        Debugger.log("Health checker shutdown complete...", null);

    }

    public void done(){interrupted = true;}


    /**
     * Creates a Timer task for Harm
     * @param timer
     * @param mapper
     * @param entry
     */
    private void addTimerTaskForHarm(Timer timer, ObjectMapper mapper, HashMap.Entry<Integer, NodeAttribute> entry) {
        if (!Thread.currentThread().isInterrupted()){
            NodeAttribute attributes = null;
//        try {
//            attributes = mapper.readValue(entry.getValue(), NodeAttribute.class);
//        } catch (IOException e) {
//            Debugger.log("", e);
//        }
            attributes = entry.getValue();
            //Debugger.log("Health Checker: Starting scheduled health task for harm node: " + attributes.getAddress(), null);
            addTimerTask(timer, entry.getKey(),
                    attributes.getAddress(),
                    null,
                    Module.HARM);
        }

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


        if (!Thread.currentThread().isInterrupted()){
            timer.scheduleAtFixedRate(new HealthCheckerTask(uuid,
                            host,
                            spaceAvailableSoFar,
                            stalker),
                    intialDelay,
                    interval);
        }
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
        private final int timeoutForReply = 1000;

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
                    NetworkUtils.closeSocket(socket);
                    if (tcpPacket != null){
                        String  content = tcpPacket.getMessage();
                        ObjectMapper mapper = new ObjectMapper();

                        // parse the packet content
                        JsonNode healthCheckReply = mapper.readTree(content);
                        Module sender = Module.valueOf(healthCheckReply.get("sender").asText());
                        String status = healthCheckReply.get("status").textValue();
                        long availableSpace =  healthCheckReply.get("diskSpace").asLong();

                        JsonNode corruptedChunksListNode = healthCheckReply.get("corruptedChunks");
                        ObjectReader reader = mapper.readerFor(new TypeReference<Set<String>>() {
                        });
// use it
                        Set<String> corruptedList = reader.readValue(corruptedChunksListNode);
                        if(status.equals("SUCCESS")){
                            if (target == Module.STALKER && this.spaceToUpdate != null) {
                                this.spaceToUpdate.set(availableSpace);
                                if (debugMode) {
                                    Debugger.log("Health Checker: Status was success for health check and disk space available. "
                                            + this.spaceToUpdate.get(), null);
                                }
                            } else if (target == Module.HARM) {
                                // need to add the space for all HARMS in config file

                                harmHistory.get(this.uuid).setSpace(availableSpace);
                                NetworkUtils.toFile(cfg.getHarm_hist_path(), harmHistory);
//                                NetworkUtils.updateHarmList(String.valueOf(this.uuid),
//                                        availableSpace,
//                                        true);
                            }
                        }
                        else if (status.equals("CORRUPT") && sender == Module.HARM) {
                            replaceChunk(socket, commsHandler, corruptedList);
                        }
                    }
                }
            }
            catch(NullPointerException e){
            }
            catch (SocketException e) {
                //(socket);
                // server has not replied within expected timeoutTime
                Debugger.log("Module of type " + target.toString() + " at " + host  + " has died!", null);
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", null);
                }

            } catch (IOException e) {
                // any other IO exception, also stop the task and assume the node is dead
                Debugger.log("Module of type " + target.toString() + " at " + host  + " has died!", null);
                updateConfigAndEndTask();
                if(debugMode) {
                    Debugger.log("", null);
                }
            }
            catch (RuntimeException  e){
                Debugger.log("Health check failed when recieveing chunks.",null);
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


        private boolean replaceChunk(Socket socket, CommsHandler commsHandler, Set<String> corruptedList){
            Debugger.log("Health check: Corrupted chunk detected on HARM: " + host + ".", null);
            if(!corruptedList.isEmpty()){
                Map<String, Set<String>> addressesOfCopies = getAddressesOfCopies(corruptedList);

                for (String uuid : addressesOfCopies.keySet()){

                    Chunk c = null;
                    CommsHandler commLink = new CommsHandler();
                    //lets get the chunk first
                    Debugger.log("Health Checker: Attempting to retrieve copy of corrupted chunk.", null);
                    for (String addr : addressesOfCopies.get(uuid)){

                        try{
                            Socket harmServer = NetworkUtils.createConnection(addr, ConfigManager.getCurrent().getHarm_listen());
                            if(commLink.sendPacket(harmServer, MessageType.DOWNLOAD, NetworkUtils.createSerializedRequest(uuid, MessageType.DOWNLOAD, ""), true) == MessageType.ACK){
                                FileStreamer fileStreamer = new FileStreamer(harmServer);
                                fileStreamer.receiveFileFromSocket(cfg.getStalker_chunk_dir() + uuid);
                                NetworkUtils.closeSocket(harmServer);
                                c = new Chunk();
                                c.setChunk_path(cfg.getStalker_chunk_dir() + uuid);
                                c.setUuid(uuid);
                                break;
                            }
                        }
                        catch (Exception e){
                            Debugger.log("Health check: could not get copy of chunk.", null);
                        }
                    }
                    if (c == null){
                        throw new RuntimeException("Health check: Could not retrieve chunks.");
                    }
                    try {

                        Debugger.log("Health Checker: Sending replacement chunk.", null);
                        socket = NetworkUtils.createConnection(host, cfg.getHarm_listen());
                        //start the replacement process
                        if(commsHandler.sendPacket(socket, MessageType.REPLACE,
                                NetworkUtils.createSerializedRequest(uuid, MessageType.REPLACE,""), true) == MessageType.ACK){
                            //send that shit
                            FileStreamer fileStreamer = new FileStreamer(socket);
                            fileStreamer.sendFileToSocket(c.getChunk_path());
                            NetworkUtils.closeSocket(socket);
                            break;
                        }
                        Debugger.log("Health check: File replaced.", null);
                    }
                    catch (Exception e){
                        Debugger.log("Health check: Error replacing chunk.", null);
                    }

                }
            }

            return(true);
        }

        private Map<String, Set<String>> getAddressesOfCopies(Set<String> corruptedList) {
            Map<String, Set<String>> harmIps = new HashMap<>();

            IndexFile indexFile = Indexer.loadFromFile();
            Map<String, List<Integer>> chunkIndex = indexFile.getChunkIndex();

            Map<Integer, NodeAttribute> harmList = NetworkUtils.getNodeMap(ConfigManager.getCurrent().getHarm_list_path());

            for (String corruptedChunkId : corruptedList) {
                Set<Integer> macIds = chunkIndex.get(corruptedChunkId).stream()
                        .filter(macId -> macId != this.uuid)  // filering out the current harm
                        .collect(Collectors.toSet());

                Set<String> addresses = new HashSet<>();

                for (int macId : macIds){
                    addresses.add(harmList.get(macId).getAddress());
                }

                harmIps.put(corruptedChunkId, addresses);


            }

            return harmIps;
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
                    NetworkUtils.toFile(cfg.getStalker_list_path(),stalkerMap);
                    Debugger.log("Leader has died.", null);
                    //send update signal
                    // kill and the threads
                    for(Map.Entry<Integer, String> entry : stalkerMap.entrySet())
                    {
                        int port = cfg.getElection_port();
                        Socket socket;
                        try {
                            socket = NetworkUtils.createConnection(entry.getValue(), port);
                            if (socket != null){
                                // create a leader packet and send it to this host
                                CommsHandler commsHandler = new CommsHandler();
                                commsHandler.sendPacketWithoutAck(socket, MessageType.REELECT, "");
                                NetworkUtils.closeSocket(socket);
                                socket.close();
                            }
                        }catch (IOException e) {
                            Debugger.log("Oh dear.. somefing wong.", null);
                        }
                    }
                    interrupted = true;
                }
                else{
                    Debugger.log("A worker has died.", null);
                }
            }else {

                harmHistory.get(this.uuid).setAlive(false);
//                Debugger.log("THE HARM IS DEAD " + harmHistory.get(this.uuid).isAlive(),null);
                // don't remove but mark as dead in HARM list and save to file
                NetworkUtils.toFile(cfg.getHarm_hist_path(), harmHistory);
            }
            //cancel task
            Debugger.log("Health Checker: Error Occurred Cancelling scheduled task for " + this.host + ".", null);
            cancel();
            Debugger.log("task cancelled...", null);
        }
    }
}
