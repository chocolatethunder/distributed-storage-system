package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Election implements Runnable {

    // updates the role in the app.java

    // HARDCODING A LIST HERE
    private static HashMap<Integer, String> stalkerMap = new HashMap<>();
    private static List<Integer> ids = new ArrayList<>();
    private static int role = -1;


    public Election()
    {
        initStalkerMap();

    }

    public void initStalkerMap() {
        for(int i = 0; i < 5; i++)
        {
            ids.add(i);
            stalkerMap.put(i,"x.x.x.x");
        }
    }

    public static int identifyRole()
    {
        // if your id is the same as the leader ids
        if(NetworkUtils.getMacID() == ids.get(0))
        {
            return 0;
        }
        // if you are the vice-leader
        else if (NetworkUtils.getMacID() == ids.get(1))
        {
            return 2;
        }
        // if you are normal request handling stalker
        else
        {
            return 1;
        }

    }

    public static int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }


    public void holdElection()
    {
        //wait for the initial election to be over
        Thread electionThread = new Thread(new Election());
        electionThread.start();
        try {
            electionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        // read the confiq file

        // get the local leader

        // broadcast this leader

        // listen for other people leader

        // agree on the leader

        // change the role of the Stalker


        // assuming ideal case
        int stalkerRole = identifyRole();
        setRole(stalkerRole);
    }
}

