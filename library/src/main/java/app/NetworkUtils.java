package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
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

    public Socket createConnection(String host, int port) throws IOException {
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
    public static Map<String, String> mapFromJson(String s){
        ObjectMapper mapper = new ObjectMapper();

        //Optional<List<String>> list = Optional.empty();
        Map<String, String> list = new HashMap<>();
        try {

            TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
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


    public static int getMacID(){    //get the mac ID of the current device
        int mac_addr = Integer.MAX_VALUE;
        //use the argvalues to add a port modifier to the HARM to differentiate it from the others
        try{

            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            Enumeration<NetworkInterface> e = network.getNetworkInterfaces();
            NetworkInterface next = e.nextElement();
            System.out.println(next.toString());
            byte[] bytes = next.getHardwareAddress();
            System.out.println(mac_addr);
            mac_addr = convertByteToInt(bytes);
        }
        catch (NullPointerException ex){
            ex.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Harm ID: " + mac_addr);
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

}
