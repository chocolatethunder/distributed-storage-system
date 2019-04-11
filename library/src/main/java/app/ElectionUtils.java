package app;

import java.util.List;

public class ElectionUtils {

    /** Identifies the role of a Stalker based on the leader and its id*/
    public static int identifyRole(List<Integer> ids,int leader)
    {
        // if your id is the same as the leader ids
        int mymac = NetworkUtils.getMacID();
        if(mymac == ConfigManager.getCurrent().getLeader_id())
        {
            return 0;
        }
        // if you are the vice-leader
        else if (NetworkUtils.getMacID() == ids.get(0))
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
