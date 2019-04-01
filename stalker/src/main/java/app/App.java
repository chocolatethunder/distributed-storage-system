/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import app.LeaderUtils.CRUDQueue;
import app.LeaderUtils.RequestAdministrator;
import app.chunk_utils.Indexer;
import app.chunk_utils.IndexFile;
import org.apache.commons.io.FilenameUtils;
import java.io.*;

public class App {

    public static void main(String[] args) {

        //First thing to do is locate all other stalkers and print the stalkers to file

        DiscoveryManager DM = new DiscoveryManager(Module.STALKER);
        DM.start();


        System.out.println("This Stalker's macID" + NetworkUtils.getMacID());
        int test = 0;
        initStalker();
        IndexFile ind = Indexer.loadFromFile();
        //ind.summary();
        System.out.println(NetworkUtils.timeStamp(1) + "Stalker Online");
        //testing

        //starting health check listener
        //ListenerThread healthCheckHandler = new ListenerThread();
        //healthCheckHandler.run();


        //election based on networkDiscovery


        while (true){
            int role = getRole();
            switch (role){
                case 0:
                    //This means that this STK is the leader
                    //create a priority comparator for the Priority queue
                    CRUDQueue syncQueue = new CRUDQueue();
                    Thread t1 = new Thread(new StalkerRequestHandler(syncQueue));
                    Thread t2 =  new Thread(new RequestAdministrator(syncQueue));
                    t1.start();
                    t2.start();

                    try{
                        t1.join();
                        t2.join();
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    Thread jcpReq = new Thread(new JcpRequestHandler(ind));
//                JcpRequestHandler jcpRequestHandler = new JcpRequestHandler(ind);
//                jcpRequestHandler.run();
                    jcpReq.start();
                    try {
                        jcpReq.join();
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    break;
            }
        }

    }

    public static int getRole(){
        return(0);
    }

    //cleans chunk folders on startup
    public static void initStalker(){
        //clear chunk folder
        File chunk_folder = new File("temp/chunks/");

        File[] chunk_folder_contents = chunk_folder.listFiles();
        File temp_folder = new File("temp/toChunk/");
        File[] temp_folder_contents = temp_folder.listFiles();

        if(chunk_folder_contents != null) {
            for (File f : chunk_folder_contents) {
                if (!FilenameUtils.getExtension(f.getName()).equals("empty")) {
                    f.delete();
                }
            }
        }

        if(temp_folder_contents != null) {
            for (File f : temp_folder_contents) {
                if (!FilenameUtils.getExtension(f.getName()).equals("empty")) {
                    f.delete();
                }
            }
        }

    }


}

