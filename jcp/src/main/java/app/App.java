/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    // to capture total disk space in the system and will be updated with each health check
    static JFrame mainFrame = new JFrame("KRATOS");
    static JList listOfFiles = new JList();
    static JTextArea consoleOutput = new JTextArea();
    static DefaultListModel listModel = new DefaultListModel();
    static RequestSender requestSender;
    public volatile int TotalDiskSpace = 0 ;
    //jcp main
    public static void main(String[] args) {
        int test  = 0;

        int discoveryTimeout = 5;
        System.out.println(NetworkUtils.timeStamp(1) + "JCP online");
        //make a discovery manager and start it, prints results to file
        //this beast will be running at all times
        Thread discManager = new Thread(new DiscoveryManager(Module.JCP, discoveryTimeout, false));
        discManager.start();
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for stalker list to update");
        try{
            Thread.sleep((discoveryTimeout * 1000) + 5000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(NetworkUtils.timeStamp(1) + "List updated!");
        initJFrame();

        System.out.println("here");
//
        //get the stalkers from file
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/stalkers.list"));
        //get sorted list from targets
        requestSender = RequestSender.getInstance();

////
////        //HealthChecker checker = new HealthChecker();
////        //checker.start(m);
////
        //ip of stalker we'll just use the one at index 1 for now
        while(true){
            try{
                Thread.sleep((10000));
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    public static Socket connectToStalker(){
        int port = 11111;
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/stalkers.list"));
        List<Integer> s_list = NetworkUtils.mapToSList(m);
        String stalkerip =  m.get(s_list.get(1));
        return(requestSender.connect(stalkerip, port));
    }
//
//    //load a config (stalker ip) from file while we get network discovery working
    public static void retrieveFiles() {
        //uncomment this:
            /*
                listModel.clear();
                List<String> fileList = requestSender.getFileList();
                for (int i=0; i < fileList.size(); i++) {
                    listModel.addElement(fileList.get(i);
                }
            */
        //remove this:
        listOfFiles.setModel(listModel);
        consoleOutput.append("Listed files.\n");
        System.out.println("Listed files.");
    }

    public static void chooseFile() {
        Socket connection = connectToStalker();
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            String name = FilenameUtils.separatorsToUnix(selectedFile.getAbsolutePath());
            System.out.println(name);
            //remove this:
            listModel.addElement(selectedFile.getName());
            //uncomment this:
            requestSender.sendFile(name);
            consoleOutput.append("Uploaded " + selectedFile + "\n");
            System.out.println("Uploaded " + selectedFile);
        }
        retrieveFiles();
        try{ connection.close();}
        catch(IOException e){ e.printStackTrace();}

    }

    public static void deleteFile() {
        Socket connection = connectToStalker();
        int index = listOfFiles.getSelectedIndex();
        Object selectedFilename = listOfFiles.getSelectedValue();
        //remove this:
        listModel.removeElement(selectedFilename);
        //remove this:
        listOfFiles.setModel(listModel);
        //uncomment this:
        requestSender.deleteFile(selectedFilename.toString());
        consoleOutput.append("Deleted " + selectedFilename.toString() + "\n");
        System.out.println("Deleted " + selectedFilename.toString());
        retrieveFiles();
        try{ connection.close();}
        catch(IOException e){ e.printStackTrace();}
    }

    public static void downloadFile() {
        Socket connection = connectToStalker();
        String selectedFilename = listOfFiles.getSelectedValue().toString();
        //remove this?:
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);
        //
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            //uncomment this:
            requestSender.getFile(selectedFile + "/" + selectedFilename);
            consoleOutput.append("Downloaded " + selectedFilename + " to " + selectedFile + "\n");
            System.out.println("Downloaded " + selectedFilename + " to " + selectedFile);
        }
        try{ connection.close();}
        catch(IOException e){ e.printStackTrace();}
    }

    public static void initJFrame(){
        String request = null;
        String filename = null;

        //set up the gui
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBounds(250,30,100,40);
        JButton listButton = new JButton("List Files");
        listButton.setBounds(250,80,100,40);
        JButton downloadButton = new JButton("Download");
        downloadButton.setBounds(50,350,100,40);
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBounds(250,350,100,40);
        JScrollPane scrollableList = new JScrollPane(listOfFiles);
        scrollableList.setBounds(50,150,300,200);
        listOfFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollableConsole = new JScrollPane(consoleOutput);
        scrollableConsole.setBounds(50,30,180,90);
        consoleOutput.setEditable(false);

        mainFrame.add(uploadButton);
        mainFrame.add(listButton);
        mainFrame.add(downloadButton);
        mainFrame.add(deleteButton);
        mainFrame.add(scrollableList);
        mainFrame.add(scrollableConsole);

        //set up listeners
        UploadListener uploadListener = new UploadListener();
        uploadButton.addActionListener(uploadListener);
        ListListener listListener = new ListListener();
        listButton.addActionListener(listListener);
        DownloadListener downloadListener = new DownloadListener();
        downloadButton.addActionListener(downloadListener);
        DeleteListener deleteListener = new DeleteListener();
        deleteButton.addActionListener(deleteListener);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400,500);
        mainFrame.setLayout(null);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);

        //bring window to front
        mainFrame.setAlwaysOnTop(true);
        mainFrame.setAlwaysOnTop(false);
    }

}
