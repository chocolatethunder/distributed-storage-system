package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigManager {

    private static ConfigFile current = null;
    private static Map<String, ConfigFile> configs;

    public ConfigManager(){}
    //load a cfg from file
    public static boolean newFile(String config_path, String config_name){
        return(loadFromFile(config_path, config_name, true));
    }
    //load from file, creates new file if bool activated
    public static boolean loadFromFile(String config_path, String configName, boolean new_if_fail){
        ConfigFile cf = deserialize(NetworkUtils.fileToString(config_path));
        if ( cf != null && cf.getConf_path().length() > 0){
            cf.setConf_path(config_path);
            configs.put(configName, cf);
            //set as current config
            current = cf;
            NetworkUtils.loadConfig(current);
            return true;
        }
        //ability to create new file if not available
        Debugger.log("Config Manager: Config file not found " + config_path, null);
        if (new_if_fail){
            Debugger.log("Config Manager: Creating new config " + config_path, null);
            cf = new ConfigFile((config_path));
            saveToFile(configName);
            configs.put(configName, cf);
            current = cf;

            return true;
        }
        return false;
    }

    //prints a cfg to file
    //takes the name mapped to a config
    public static void saveToFile(String config_name){
        ConfigFile cfg = configs.get(config_name);
        NetworkUtils.toFile(cfg.getConf_path(), cfg);
    }

    //takes a serialized json and convert to cfg
    public static ConfigFile deserialize(String  s){
            ObjectMapper mapper = new ObjectMapper();
            Optional<ConfigFile> cfg = Optional.empty();
            try {
                cfg = Optional.of(mapper.readValue(s, ConfigFile.class));
            } catch (IOException e) {
                Debugger.log("", e);
                return null;
            }
            return cfg.get();
    }

    public static ConfigFile getCurrent(){return(current);}
    public boolean setCurrent(String identifier){
        ConfigFile cfg = configs.get(identifier);
        if (cfg != null){
            current = cfg;
            Debugger.log("Config Manager: configuration set to " + identifier, null);
            return true;
        }
        Debugger.log("Config Manager: couldn't find config with identifier " + identifier, null);
        return false;
    }

}
