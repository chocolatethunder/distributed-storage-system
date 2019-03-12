/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    public static void main(String[] args) {

        //initialize socket and input stream
        Socket socket = null;
        ServerSocket server = null;
        DataInputStream in = null;
        DataOutputStream out = null;


        int bytesRead;

        // we can change this later to increase or decrease
        ExecutorService executorService = Executors.newFixedThreadPool(10);


        try {
            server = new ServerSocket(6555);

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Waiting...");

        // will keep on listening for requests
        while (true) {

            try {

                socket = server.accept();
                System.out.println("Accepted connection : " + socket);


                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());


                Optional<TcpPacket> packet = executeHandshake(in, out);


                executorService.execute(new Handler(socket, packet.get()));



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                try {
//                    in.close();
//                    out.close();
//                    socket.close();
//                } catch (IOException i) {
//                    i.printStackTrace();
//                }
            }

        }
    }

    /**
     *
     * @param in
     * @param out
     * @return
     * @throws IOException
     */
    private static Optional<TcpPacket> executeHandshake(DataInputStream in, DataOutputStream out) throws IOException {
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


        return receivedPacket;


    }
}
