package app;

import app.RequestType;
import app.TcpPacket;
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


    /**
     *
     * @param in
     * @param out
     * @return
     * @throws IOException
     */

    private static TcpPacket executeHandshake(DataInputStream in, DataOutputStream out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Optional<TcpPacket> receivedPacket = Optional.empty();

        try {
            String rec = in.readUTF();
            receivedPacket = Optional.of(mapper.readValue(rec, TcpPacket.class));

        } catch (EOFException e) {
        }


        //TO:Do need actual logic here if the server is busy or available depending on the type of Request

        TcpPacket sendAvail = new TcpPacket(RequestType.UPLOAD, "AVAIL");

        String jsonInString = mapper.writeValueAsString(sendAvail);
        System.out.println(jsonInString);
        out.writeUTF(jsonInString);


        return receivedPacket.get();


    }


    /**
     *
     */
    @Override
    public void run() {

        //initialize socket and input stream
        Socket socket = null;
        ServerSocket server = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        BufferedOutputStream bufferedOutputStream = null;


        int bytesRead;

        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);


        try {
            server = new ServerSocket(6553);

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Waiting...");
        String fname = "";
        // will keep on listening for requests
        while (true) {

            try {

                socket = server.accept();
                System.out.println("Accepted connection : " + socket);


                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());


                TcpPacket req = executeHandshake(in, out);
                // receive file in chunks



                executorService.execute(ServiceHandlerFactory.getServiceHandler(RequestType.valueOf(req.getRequestType()), socket,req.getFileName()));



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

