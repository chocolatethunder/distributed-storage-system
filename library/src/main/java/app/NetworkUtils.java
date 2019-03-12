package app;

import java.io.IOException;
import java.net.Socket;

/**
 *
 */
public class NetworkUtils {

    public Socket createConnection(String host, int port) throws IOException {
        Socket socket = null;

        // establish a connection
        //TO:DO Need logic for getting the stalker in round robin fashion
        socket = new Socket(host, port);
        System.out.println("Connected");
        return socket;
    }



}
