package app;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 *This class is responsible for scheduling task for doing health checks on all units in the config file
 */
public class HealthChecker {

    // every 5 seconds for now
    private final long interval = 1000 * 5;

    public void start(HashMap<Integer, String> stalkers) {

        Timer timer = new Timer();

        // for each node in the list, scheduling a task to occur at interval
        for(Map.Entry<Integer, String> entry : stalkers.entrySet()) {
            System.out.println("Starting scheduled health task for node: " + entry.getValue());
            timer.scheduleAtFixedRate(new HealthCheckerTask(entry.getValue()), 0, interval);
        }


    }

    class HealthCheckerTask extends TimerTask {

        private final String host ;
        private final int port = 11114;
        private final int timeoutForReply = 1000 * 5;

        HealthCheckerTask( String host){
            this.host = host;

        }

        @Override
        public void run() {
            System.out.println("Health Checker Task for host "+ host + ": " + NetworkUtils.timeStamp(1));
            Socket socket = null;
            try {

                socket = NetworkUtils.createConnection(host, port);

                //if server does not reply within 5 seconds, then SocketException will be thrown
                socket.setSoTimeout(1000 * timeoutForReply);


                CommsHandler commsHandler = new CommsHandler();
                //sending the health check request
                commsHandler.sendPacketWithoutAck(socket, MessageType.HEALTH_CHECK, "request");


                //receive packet from node
                TcpPacket tcpPacket = commsHandler.receivePacket(socket);
                String  content = tcpPacket.getMessage();
                System.out.println(content);

            } catch (SocketException e) {

                // server has not replied within expected timeoutTime
                e.printStackTrace();
                cancel();
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
}
