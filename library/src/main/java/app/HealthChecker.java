package app;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class HealthChecker {

    private final long interval = 1000 * 10;

    public void start() {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new HealthCheckerTask("127.0.0.1"), 0, interval);


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

                socket = NetworkUtils.createConnection("127.0.0.1", port);
                socket.setSoTimeout(1000 * timeoutForReply);

                CommsHandler commsHandler = new CommsHandler();
                commsHandler.sendPacket(socket, MessageType.HEALTH_CHECK, "request");

                TcpPacket tcpPacket = commsHandler.receivePacket(socket);

                if (tcpPacket.getMessage() == "success") {
                    System.out.println("health check was successfull");
                }

            } catch (SocketException e) {

                // server has not replied within expected timeoutTime
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
