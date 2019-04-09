package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigManager {

    private static ConfigFile current = null;
    private static Map<String, ConfigFile> configs = new HashMap<>();

    public ConfigManager(){}
    //load a cfg from file
    public static boolean newFile(String config_path, String config_name){
        return(loadFromFile(config_path, config_name, true));
    }
    //load from file, creates new file if bool activated
    public static boolean loadFromFile(String config_path, String configName, boolean new_if_fail){
        ConfigFile cf = null;
        String file_contents = NetworkUtils.fileToString(config_path);
        if (file_contents != null){
            Debugger.log(file_contents, null);
            cf = deserialize(file_contents);
        }
        if ( cf != null && cf.getConf_path().length() > 0){
            cf.setConf_path(config_path);
            configs.put(configName, cf);
            //set as current config
            current = cf;
            NetworkUtils.loadConfig(current);
            Debugger.log("Config Manager: Config loaded from file: " + config_path, null);
            return true;
        }
        //ability to create new file if not available
        Debugger.log("Config Manager: Config file not found " + config_path, null);
        if (new_if_fail){
            Debugger.log("Config Manager: Creating new config " + config_path, null);
            cf = new ConfigFile(config_path);
            File f = new File(config_path);
            Debugger.log(cf.getHarm_list_path() + "", null);
            try {
                f.createNewFile();
                saveToFile(cf);
            }
            catch(IOException e){
                Debugger.log("", e);
            }
            catch(NullPointerException e){
                Debugger.log("", e);
            }
            configs.put(configName, cf);
            current = cf;

            return true;
        }
        return false;
    }

    //prints a cfg to file
    //takes the name mapped to a config
    public static void saveToFile(ConfigFile cfg){
        //ConfigFile cfg = configs.get(config_name);
//        Debugger.log("Config Manager: debug  1: " + cfg.getConf_path(), null);
        NetworkUtils.toFile(cfg.getConf_path(), cfg, true);
    }

    //takes a serialized json and convert to cfg
    public static ConfigFile deserialize(String  s){
            ObjectMapper mapper = new ObjectMapper();
            ConfigFile cfg = null;
            try {
                cfg = mapper.readValue(s, ConfigFile.class);
            } catch (IOException e) {
                Debugger.log("", e);
                return null;
            }
            catch (NullPointerException ex){
                Debugger.log("", ex);
                return null;
            }
            return cfg;
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
