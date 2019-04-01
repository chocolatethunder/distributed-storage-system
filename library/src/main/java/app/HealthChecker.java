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
public class HealthChecker {

    // every 5 seconds for now
    private final long interval = 1000 * 5;
    private Map<Integer, String> stalkerList;
    private Map<Integer, String> harmList;
    private AtomicLong spaceAvailableSoFar;


    /**
     * This constructor is meant to be used from JCP to do health checks on STALKERS
     * @param stalkers
     * @param spaceAvailableSoFar
     */
    public HealthChecker(Map<Integer, String> stalkers, AtomicLong spaceAvailableSoFar){
        stalkerList = stalkers;
        this.spaceAvailableSoFar = spaceAvailableSoFar;
    }


    /**
     * This constructor should be used from STALKER units to do health checks on other STALKERS and HARM lists
     * @param stalkers
     * @param harms
     * @param spaceAvailableSoFar
     */
    public HealthChecker(Map<Integer, String> stalkers,
                         Map<Integer, String> harms,
                         AtomicLong spaceAvailableSoFar){
        this.stalkerList = stalkers;
        this.harmList = harms;
        this.spaceAvailableSoFar = spaceAvailableSoFar;
    }


    /**
     * This method schedules TimerTask for each node in the config file
     * TimerTask is a runnable that executes run method at fixed interval
     */
    public void startTask() {

        Timer timer = new Timer();

        // for each node in the stalker list, scheduling a task to occur at interval
        for(Map.Entry<Integer, String> entry : stalkerList.entrySet()) {
            System.out.println("Starting scheduled health task for node: " + entry.getValue());
            timer.scheduleAtFixedRate(new HealthCheckerTask(entry.getKey(),
                    entry.getValue(),
                    spaceAvailableSoFar,
                    Module.STALKER),
                    0,
                    interval);
        }

        if(harmList != null) {
            // for each node in the harm list, scheduling a task to occur at interval
            for (Map.Entry<Integer, String> entry : harmList.entrySet()) {
                System.out.println("Starting scheduled health task for node: " + entry.getValue());
                timer.scheduleAtFixedRate(new HealthCheckerTask(entry.getKey(),
                        entry.getValue(),
                        spaceAvailableSoFar,
                        Module.HARM),
                        0,
                        interval);
            }
        }


    }


    /**
     ******This is a placeholder method, not sure how to use it yet
     * Should be used to restart health check for a newly found node after initial discovery
     * @param stalkerIp
     * @param target
     */
    public void restartTask(String stalkerIp, Module target) {

        Timer timer = new Timer();

        System.out.println("Starting scheduled health task for node: " + stalkerIp);
        //NOT CORRECT
        timer.scheduleAtFixedRate(new HealthCheckerTask(0, stalkerIp, null, target), 0, interval);

    }




    class HealthCheckerTask extends TimerTask {

        private final String host ;
        private final int uuid;
        private final int port = 11114;
        private final int timeoutForReply = 1000 * 5;
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

                socket = NetworkUtils.createConnection("127.0.0.1", Integer.valueOf(host));

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
                    if(target == Module.STALKER) {
                        this.spaceToUpdate.set(availableSpace);
                        System.out.println("Status was success for health check and disk space available "
                                + this.spaceToUpdate.get());
                    }else{
                        // need to add the space for all HARMS
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
            }

            //cancel task
            System.out.println("Cancelling scheduled task for " + this.host);
            cancel();
        }
    }
}
