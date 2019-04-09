package app;

public class ConfigFile {
    private String conf_path;



    private boolean random = false;

    private int chunk_count = 5;
    private int replica_count = 10;



    private int election_threshold_s = 3;
    private int election_threshold_h = 3;

    private int stalker_update_freq = 20;
    private int harm_update_freq = 10;
    private int jcp_update_freq = 5;

    private int time_to_reboo = 60000;

    private int STK_JCP = 10000;
    private int JCP_STK = 11000;

    private int STK_HARM = 10001;
    private int HARM_STK = 11001;

    private int STK_STK_S = 10002;
    private int STK_STK_R = 11002;


    private int jcp_req_port = 11111;
    private int leader_report = 11112;

    private int leader_admin_port = 11113;
    //health check as welll
    private int election_port = 11114;
    private int harm_listen = 22222;

    private int debug_mode = 3;


    private String harm_list_path = "index/lists/harm.list";
    private String stalker_list_path = "index/lists/harm.list";
    private String index_file_path = "index/index_file/main.index";
    private String config_path = "config/";

    public ConfigFile(){}
    public ConfigFile(String config_path){
        this.conf_path = config_path;
    }


    public String getConf_path() { return conf_path; }
    public void setConf_path(String conf_path) { this.conf_path = conf_path; }
    public boolean isRandom() { return random; }
    public void setRandom(boolean random) { this.random = random; }


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

    public int getElection_port() {
        return election_port;
    }

    public void setElection_port(int election_port) {
        this.election_port = election_port;
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
    public int getLeader_admin_port() {
        return leader_admin_port;
    }

    public void setLeader_admin_port(int leader_admin_port) {
        this.leader_admin_port = leader_admin_port;
    }
}
