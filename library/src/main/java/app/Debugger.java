package app;
import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.exception.ExceptionUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.StringWriter;
import java.io.PrintWriter;

public class Debugger {

    public static boolean initialized = false;
    private static int mode = 0;
    private static boolean to_file = false;
    private static boolean console = true;
    private static String logDir = "logs/";
    public Debugger(){}
    public static String traceString(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return(sw.toString());
    }

    public static synchronized void log(String message, Exception e){
        String s = "";
        switch (mode){
            case 0:
                //off
                break;
            case 1:
                //print message
                if (!message.equals("")){
                    s = NetworkUtils.timeStamp(1) + message;
                }
                break;
            case 2:
                //print stacktrace
                if (e != null){
                    s = NetworkUtils.timeStamp(1) + traceString(e);
                }
                break;
            case 3:
                if (e!=null){
                    if (message.equals("")){
                        s = NetworkUtils.timeStamp(1) + traceString(e);
                    }
                    else{
                        s = NetworkUtils.timeStamp(1) + traceString(e) + "\n\n" + NetworkUtils.timeStamp(1) + message;
                    }
                }
                else{
                    if (!message.equals("")){
                        s = NetworkUtils.timeStamp(1) + message;
                    }

                }
                //print stack and message
                break;
            default:
                break;
        }
        if(console){
            System.out.println(s);
        }
        if (to_file){
            appendToLog(s + "\n\n");
        }
    }

    public static synchronized void appendToLog(String s){
        Charset c = null;
        try{
            if (!initialized){
                FileUtils.writeStringToFile(new File(logDir + "output.log"), "\n\n" + NetworkUtils.timeStamp(0) + "\n", c, true);
                initialized = true;
            }
            FileUtils.writeStringToFile(new File(logDir + "output.log"), s, c, true);
        }
        catch (IOException e){
        }

    }
    public static void setMode(int m){
        mode = m;
    }
    public static void toggleFileMode(){
        to_file = !to_file;
    }
    public static void toggleConsoleOutput(){
        console = !console;
    }




}
