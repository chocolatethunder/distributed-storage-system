package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    //print an object to a file as json
    public static boolean toFile(String path, List<String> s){
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




}
