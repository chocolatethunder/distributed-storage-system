package app;

import java.util.List;

public class ElectionUtils {

    /** Identifies the role of a Stalker based on the leader and its id*/
    public static int identifyRole(List<Integer> ids)
    {
        // if your id is the same as the leader ids
        if(ids.get(0) == NetworkUtils.getMacID())
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

}
