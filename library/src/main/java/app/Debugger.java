package app;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Debugger {

    public static boolean initialized = false;
    private static int mode;
    private static boolean to_file = false;
    private static String logDir = "logs/";
    public Debugger(){}
    public Debugger(int mode){
        this.mode = mode;
    }
    public Debugger(int mode, boolean to_file){
        this.mode = mode;
        this.to_file = to_file;
    }


    public static synchronized void log(String message, Exception e){
        switch (mode){
            case 0:
                //off
                break;
            case 1:
                //print message
                System.out.println(message);
                break;
            case 2:
                //print stacktrace
                e.printStackTrace();
                break;
            case 4:
                System.out.println(message);
                e.printStackTrace();
                //print stack and message
                break;
            default:
                break;
        }
        if (to_file){
            String toPrint = NetworkUtils.timeStamp(1) +  e.toString() + "\n"  + NetworkUtils.timeStamp(1) + message + "\n\n";
            if (e != null){
                toPrint += NetworkUtils.timeStamp(1) +  e.toString() + "\n";
            }
            appendToLog(toPrint);
        }
    }

    public static synchronized void appendToLog(String s){
        Charset c = null;
        try{
            if (!initialized){
                FileUtils.writeStringToFile(new File(logDir + "log.txt"), "\n\n" + NetworkUtils.timeStamp(0) + "\n", c, true);
                initialized = true;
            }
            FileUtils.writeStringToFile(new File(logDir + "log.txt"), s, c, true);
        }
        catch (IOException e){
        }

    }

    public static void setMode(int m){
        mode = m;
    }




}
