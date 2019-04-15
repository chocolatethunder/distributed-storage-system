package app;

public class ConfigFile {
    private String conf_path;
    private int leader_id;
    private boolean reelection = false;
    private int role = 0;

    private boolean random = false;

    private int chunk_count = 5;
    private int replica_count = 3;

    private String broadcast_ip = "192.168.0.255";

    private int election_threshold_s = 3;
    private int election_threshold_h = 3;

    private int stalker_update_freq = 3;
    private int harm_update_freq = 3;
    private int jcp_update_freq = 1;

    private int time_to_reboo = 60000;

    private int STK_JCP = 10000;
    private int JCP_STK = 11000;

    private int STK_HARM = 10001;
    private int HARM_STK = 11001;

    private int STK_STK_S = 10002;
    private int STK_STK_R = 11002;


    private int jcp_req_port = 50000;
    private int leader_report = 50001;

    private int leader_admin_port = 50002;
    private int election_port = 50003;
    private int health_check_port = 50004;
    private int harm_listen_port = 50005;


    //the port for requests
    private int harm_listen = 50006;

    private int debug_mode = 3;

    private String harm_list_path = "index/lists/harm.list";
    private String harm_hist_path = "index/lists/harm_hist.list";
    private String stalker_list_path = "index/lists/stalker.list";
    private String index_file_path = "index/index_file/main.index";
    private String config_path = "config/";

    private String stalker_temp_dir = "temp";
    private String stalker_assembled_dir = "temp/assembled";
    private String stalker_toChunk_dir = "temp/toChunk";
    private String stalker_chunk_dir = "temp/chunks";



    public ConfigFile() {
    }

    public String getHarm_hist_path() { return harm_hist_path; }

    public void setHarm_hist_path(String harm_hist_path) { this.harm_hist_path = harm_hist_path; }

    public boolean isReelection() {
        return reelection;
    }

    public void setReelection(boolean reelection) {
        this.reelection = reelection;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
    public int getHealth_check_port() {
        return health_check_port;
    }

    public void setHealth_check_port(int health_check_port) {
        this.health_check_port = health_check_port;
    }
    public String getBroadcast_ip() {
        return broadcast_ip;
    }

    public void setBroadcast_ip(String broadcast_ip) {
        this.broadcast_ip = broadcast_ip;
    }

    public int getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(int leader_id) {
        this.leader_id = leader_id;
    }

    public ConfigFile(String config_path) {
        this.conf_path = config_path;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public String getConf_path() {
        return conf_path;
    }

    public void setConf_path(String conf_path) {
        this.conf_path = conf_path;
    }

    public int getChunk_count() {
        return chunk_count;
    }

    public void setChunk_count(int chunk_count) {
        this.chunk_count = chunk_count;
    }

    public int getReplica_count() {
        return replica_count;
    }

    public void setReplica_count(int replica_count) {
        this.replica_count = replica_count;
    }

    public int getElection_threshold_s() {
        return election_threshold_s;
    }

    public void setElection_threshold_s(int election_threshold_s) {
        this.election_threshold_s = election_threshold_s;
    }

    public int getElection_threshold_h() {
        return election_threshold_h;
    }

    public void setElection_threshold_h(int election_threshold_h) {
        this.election_threshold_h = election_threshold_h;
    }

    public int getStalker_update_freq() {
        return stalker_update_freq;
    }

    public void setStalker_update_freq(int stalker_update_freq) {
        this.stalker_update_freq = stalker_update_freq;
    }

    public int getHarm_update_freq() {
        return harm_update_freq;
    }

    public void setHarm_update_freq(int harm_update_freq) {
        this.harm_update_freq = harm_update_freq;
    }

    public int getJcp_update_freq() {
        return jcp_update_freq;
    }

    public void setJcp_update_freq(int jcp_update_freq) {
        this.jcp_update_freq = jcp_update_freq;
    }

    public int getTime_to_reboo() {
        return time_to_reboo;
    }

    public void setTime_to_reboo(int time_to_reboo) {
        this.time_to_reboo = time_to_reboo;
    }

    public int getSTK_JCP() {
        return STK_JCP;
    }

    public void setSTK_JCP(int STK_JCP) {
        this.STK_JCP = STK_JCP;
    }

    public int getJCP_STK() {
        return JCP_STK;
    }

    public void setJCP_STK(int JCP_STK) {
        this.JCP_STK = JCP_STK;
    }

    public int getSTK_HARM() {
        return STK_HARM;
    }

    public void setSTK_HARM(int STK_HARM) {
        this.STK_HARM = STK_HARM;
    }

    public int getHARM_STK() {
        return HARM_STK;
    }

    public void setHARM_STK(int HARM_STK) {
        this.HARM_STK = HARM_STK;
    }

    public int getSTK_STK_S() {
        return STK_STK_S;
    }

    public void setSTK_STK_S(int STK_STK_S) {
        this.STK_STK_S = STK_STK_S;
    }

    public int getSTK_STK_R() {
        return STK_STK_R;
    }

    public void setSTK_STK_R(int STK_STK_R) {
        this.STK_STK_R = STK_STK_R;
    }

    public int getJcp_req_port() {
        return jcp_req_port;
    }

    public void setJcp_req_port(int jcp_req_port) {
        this.jcp_req_port = jcp_req_port;
    }

    public int getLeader_report() {
        return leader_report;
    }

    public void setLeader_report(int leader_report) {
        this.leader_report = leader_report;
    }

    public int getLeader_admin_port() {
        return leader_admin_port;
    }

    public void setLeader_admin_port(int leader_admin_port) {
        this.leader_admin_port = leader_admin_port;
    }

    public int getElection_port() {
        return election_port;
    }

    public void setElection_port(int election_port) {
        this.election_port = election_port;
    }

    public int getHarm_listen_port() {
        return harm_listen_port;
    }

    public void setHarm_listen_port(int harm_listen_port) {
        this.harm_listen_port = harm_listen_port;
    }

    public int getHarm_listen() {
        return harm_listen;
    }

    public void setHarm_listen(int harm_listen) {
        this.harm_listen = harm_listen;
    }

    public int getDebug_mode() {
        return debug_mode;
    }

    public void setDebug_mode(int debug_mode) {
        this.debug_mode = debug_mode;
    }

    public String getHarm_list_path() {
        return harm_list_path;
    }

    public void setHarm_list_path(String harm_list_path) {
        this.harm_list_path = harm_list_path;
    }

    public String getStalker_list_path() {
        return stalker_list_path;
    }

    public void setStalker_list_path(String stalker_list_path) {
        this.stalker_list_path = stalker_list_path;
    }

    public String getIndex_file_path() {
        return index_file_path;
    }

    public void setIndex_file_path(String index_file_path) {
        this.index_file_path = index_file_path;
    }

    public String getConfig_path() {
        return config_path;
    }

    public void setConfig_path(String config_path) {
        this.config_path = config_path;
    }

    public String getStalker_temp_dir() {
        return stalker_temp_dir;
    }

    public void setStalker_temp_dir(String stalker_temp_dir) {
        this.stalker_temp_dir = stalker_temp_dir;
    }

    public String getStalker_assembled_dir() {
        return stalker_assembled_dir;
    }

    public void setStalker_assembled_dir(String stalker_assembled_dir) {
        this.stalker_assembled_dir = stalker_assembled_dir;
    }

    public String getStalker_toChunk_dir() {
        return stalker_toChunk_dir;
    }

    public void setStalker_toChunk_dir(String stalker_toChunk_dir) {
        this.stalker_toChunk_dir = stalker_toChunk_dir;
    }

    public String getStalker_chunk_dir() {
        return stalker_chunk_dir;
    }

    public void setStalker_chunk_dir(String stalker_chunk_dir) {
        this.stalker_chunk_dir = stalker_chunk_dir;
    }
}