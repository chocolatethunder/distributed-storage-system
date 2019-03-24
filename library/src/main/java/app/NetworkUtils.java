package app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


/**
 *
 */
public class NetworkUtils {

    private static int STK_JCP = 10000;
    private static int JCP_STK = 11000;

    private static int STK_HARM = 10001;
    private static int HARM_STK = 11001;

    private static int STK_STK_S = 10002;
    private static int STK_STK_R = 11002;

    public static int[] getPortTargets(String origin, String target){
        int[] ports;
        if (origin == "STALKER" && target == "STALKER"){
            ports = new int[]{STK_STK_S, STK_STK_R};

        }
        else if (origin == "STALKER" && target == "HARM"){
            ports = new int[]{STK_HARM, HARM_STK};
        }
        else if (origin == "JCP" && target == "STALKER"){
            ports = new int[]{STK_JCP, JCP_STK};
        }
        else{
            ports = new int[]{0,0};
        }
        return ports;
    }

    public static Socket createConnection(String host, int port) throws IOException {
        Socket socket = null;

        // establish a connection
        //TO:DO Need logic for getting the stalker in round robin fashion
        socket = new Socket(host, port);
        System.out.println("Connected");
        return socket;
    }

    //turn a file to string to be read by objectmapper
    public static String fileToString(String fileName) {
        String fileString = "";
        try{
            fileString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        }
        catch (IOException e){
            e.printStackTrace();
            return(null);
        }
        catch (NullPointerException ex){
            return(null);
        }
        return fileString;
    }

    //load a config (stalker ip) from file while we get network discovery working
    public static List<String> listFromJson(String s){
        ObjectMapper mapper = new ObjectMapper();
        Optional<List<String>> list = Optional.empty();
        try {
           list = Optional.of(mapper.readValue(s, List.class));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return list.get();
    }

    //load a config (stalker ip) from file while we get network discovery working
    public static HashMap<Integer, InetAddress> mapFromJson(String s){
        ObjectMapper mapper = new ObjectMapper();

        //Optional<List<String>> list = Optional.empty();
        HashMap<Integer, InetAddress> list = new HashMap<>();
        try {

            TypeReference<HashMap<Integer,Object>> typeRef = new TypeReference<HashMap<Integer,Object>>() {};
            list = mapper.readValue(s, typeRef);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }

    //print an object to a file as json
    public static boolean toFile(String path, Object s){
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(s);
            PrintWriter out = new PrintWriter(path);
            out.print(jsonInString);
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // gets the MAC address of the System
    public static int getMacID(){    //get the mac ID of the current device
        int mac_addr = Integer.MAX_VALUE;
        //use the argvalues to add a port modifier to the HARM to differentiate it from the others
        try{

            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            Enumeration<NetworkInterface> e = network.getNetworkInterfaces();

            while (e.hasMoreElements())
            {
                NetworkInterface next = e.nextElement();

                byte[] bytes = next.getHardwareAddress();
                if( bytes != null)
                {
                    mac_addr = convertByteToInt(bytes);
                    break;
                }
            }
        }
        catch (NullPointerException ex){
            ex.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return(mac_addr);
    }

    ///get an integer from a MAC addr
    public static int convertByteToInt(byte[] b) throws NullPointerException
    {
        int value= 0;
        for(int i=0; i<b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }

    public static boolean checkFile(String filename){
        File f = new File(filename);
        if (f.exists()){
            return(true);
        }
        else{
            return(false);
        }
    }

    //returns a request from the tcp packet contents
    public static Request getPacketContents(TcpPacket t){
        ObjectMapper mapper = new ObjectMapper();
        Optional<Request> r = Optional.empty();
        try{
            r =  Optional.of(mapper.readValue(t.getMessage(), Request.class));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return r.get();
    }

    //Create a serialized request to be sent with a TCP packet
    public static String createSerializedRequest(String filename, MessageType m){
        String serialRequest = null;
        ObjectMapper mapper = new ObjectMapper();
        Request r;
        if (m == MessageType.UPLOAD){
            File f = new File(filename);
            int fileSize = (int) f.length();
            r = new Request(filename,m, fileSize);
        }
        else{
            r = new Request(filename,m);
        }
        try{
            serialRequest =  mapper.writeValueAsString(r);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return serialRequest;
    }

    // returns the IP of the System that can be used for internet packet transfer
    public static String getIP() {
        InetAddress inetAddress;
        String myIP = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            myIP = inetAddress.getHostAddress();
            boolean found = false;
            InetAddress ip = InetAddress.getLocalHost();

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            Enumeration<NetworkInterface> e = network.getNetworkInterfaces();
            while (e.hasMoreElements() || !found)
            {
                NetworkInterface next = e.nextElement();
                Enumeration<InetAddress> i = next.getInetAddresses();
                while (i.hasMoreElements()){
                    InetAddress n = i.nextElement();
                    //System.out.println("IP:   " + n.getHostAddress());
                    if(n.getHostAddress().startsWith("192"))
                    {
                        myIP = n.getHostAddress();
                        found = true;
                        break;
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return myIP;
    }

}
