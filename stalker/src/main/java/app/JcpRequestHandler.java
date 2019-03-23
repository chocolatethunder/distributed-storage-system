package app;

import app.chunk_utils.IndexFile;
import app.CommsHandler;
import app.handlers.ServiceHandlerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *This is main thread that receives request through TCP from JCP
 */
public class JcpRequestHandler implements Runnable {

     private final int serverPort = 11111;
     private IndexFile index;
     public JcpRequestHandler(IndexFile ind){
         this.index = ind;
     }
    /**
     * This execute the initial handshake with JCP. It should check if it can handle request type
     * then send reply back with AVAIL | BUSY
     * @param in
     * @param out
     * @return received TCP packet from JCP
     * @throws IOException
     */

//    private static TcpPacket executeHandshake(DataInputStream in, DataOutputStream out) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        Optional<TcpPacket> receivedPacket = Optional.empty();
//
//        try {
//            String rec = in.readUTF();
//            // reading the packet as object from json string
//            receivedPacket = Optional.of(mapper.readValue(rec, TcpPacket.class));
//
//        } catch (EOFException e) {
//        }
//        //TO:Do need actual logic here if the server is busy or available depending on the type of Request
//        TcpPacket sendAvail = new TcpPacket(MessageType.UPLOAD, "AVAIL");
//
//        String jsonInString = mapper.writeValueAsString(sendAvail);
//        System.out.println(jsonInString);
//        out.writeUTF(jsonInString);
//        return receivedPacket.get();
//    }

    /**
     *This is the main run method for this thread. It run in a while loop to receive connections from
     * JCP/s  and then executes handshake. Then it spawns a thread to handle the request.
     */
    @Override
    public void run() {

        //initialize socket and input stream
        //Socket socket = null;
        ServerSocket server = null;
        CommsHandler commLink = new CommsHandler();

//        DataInputStream in = null;
//        DataOutputStream out = null;


        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            server = new ServerSocket(serverPort);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Waiting...");
        // will keep on listening for requests
        while (true) {
            try {
                //accept connection from a JCP
                Socket socket = server.accept();
                System.out.println("Accepted connection : " + socket);
                // receive packet on the socket link
                TcpPacket req = commLink.recievePacket(socket);

                //creating a specific type of service handler using factory method
                //Submit a task to the handler queue and move on
                executorService.submit(ServiceHandlerFactory.getServiceHandler(req, socket, index));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                try {
//                    in.close();
//                    out.close();
//                    socket.close();
//                    bufferedOutputStream.close();
//                } catch (IOException i) {
//                    i.printStackTrace();
//                }
            }

        }
    }


}

