/*
 * Used to hold file metadata as well as a chunklist
 */
package app;
import java.io.*;
import java.util.*;

import app.LeaderUtils.QueueEntry;
import app.chunk_utils.*;
import app.TcpPacket;
import java.net.Socket;
public class Tester {
    //temp function for testing the fileDistributor

    public void test() {
        //create a priority comparator for the queue
        Comparator<QueueEntry> entryPriorityComparator = new Comparator<QueueEntry>() {
            @Override
            public int compare(QueueEntry q1, QueueEntry q2) {
                return q1.getPriority() - q2.getPriority();
            }
        };
        PriorityQueue<QueueEntry> syncQueue = new PriorityQueue<>(entryPriorityComparator);

        Socket s1 = new Socket();
        TcpPacket t1 = new TcpPacket(MessageType.UPLOAD, "upload 1");
        TcpPacket t2 = new TcpPacket(MessageType.UPLOAD, "upload 2");
        TcpPacket t3 = new TcpPacket(MessageType.DOWNLOAD, "Down 1");
        TcpPacket t4 = new TcpPacket(MessageType.DOWNLOAD, "Down 2");
        TcpPacket t5 = new TcpPacket(MessageType.DELETE, "Del 1");
        TcpPacket t6 = new TcpPacket(MessageType.DELETE, "del 2");

        syncQueue.add(new QueueEntry(t6, s1));
        syncQueue.add(new QueueEntry(t3, s1));
        syncQueue.add(new QueueEntry(t1, s1));
        syncQueue.add(new QueueEntry(t5, s1));
        syncQueue.add(new QueueEntry(t4, s1));
        syncQueue.add(new QueueEntry(t2, s1));


        while (!syncQueue.isEmpty()){
            System.out.println(syncQueue.remove().messageString());
        }

    }

    public void cleanChunks(List<String> h_list, String chunk_dir) {
        File folder = new File(chunk_dir);
        File[] flist = folder.listFiles();
        if (flist.length != 0) {
            for (int i = 0; i < flist.length; i++) {
                flist[i].delete();
            }

            for(String s : h_list){
                folder = new File(s);
                flist = folder.listFiles();
                for (int i = 0; i < flist.length; i++) {
                    flist[i].delete();
                }
            }
        }

    }

}
