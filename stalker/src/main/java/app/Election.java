package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Election {

    // updates the role in the app.java

    // HARDCODING A LIST HERE
    HashMap<Integer, String> stalkerMap = new HashMap<>();
    List<Integer> ids = new ArrayList<>();

    public void initStalkerMap() {
        for(int i = 0; i < 5; i++)
        {
            ids.add(i);
            stalkerMap.put(i,"x.x.x.x");
        }
    }

    public Election()
    {
        initStalkerMap();

        // get the local leader
        int ledaer = ids.get(0);

        // broadcast this leader

        // listen for other people leader

        // agree on the leader

        // change the role of the Stalker


    }





}

