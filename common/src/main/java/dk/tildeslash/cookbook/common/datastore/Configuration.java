package dk.tildeslash.cookbook.common.datastore;

import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private static Map<String, String> parameters = new HashMap<>();
    private static final File DEFAULT_CONFIG_FILE = new File("cookbook.conf");

    private static final Logger LOGGER = Logger.getLogger(Configuration.class);

    /**
     * Loads the configuration for the cookbook from the default
     * configuration file (cookbook.conf)
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if there is an error while reading the file.
     */
    public static void load() throws NotConfiguredException {
        try{
            load(DEFAULT_CONFIG_FILE);
        }
        catch(IOException e){
            LOGGER.error("IOException while loading default configuration file");
            LOGGER.error(e.getMessage());
            throw new NotConfiguredException(e.getMessage());
        }
    }

    /**
     * Loads parameters from the configuration file.
     * It stores the parameters in the parameters HashSet.
     * If the configuration file does not exists, Configuration
     * tries to load the default configuration file (cookbook.conf)
     *
     * @param configFile the configuration file for the cookbook.
     * @throws IOException if there is an error while reading the file.
     */
    public static void load(File configFile) throws IOException{
        LOGGER.debug("configFile: " + configFile);
        if(configFile.exists()){
            configure(configFile);
        } else {
            LOGGER.warn("Supplied config file (" + configFile.getCanonicalPath() + ") does not exists, loading default configuration file");
            configure(DEFAULT_CONFIG_FILE);
        }
    }

    /**
     * Loads parameters from the configuration file.
     * It stores the parameters in the parameters HashSet.
     *
     * @param configFile the configuration file for the cookbook.
     * @throws IOException if there is an error while reading the file.
     */
    private static void configure(File configFile)
            throws IOException{

        try (
                FileReader fr = new FileReader(configFile);
                BufferedReader reader = new BufferedReader(fr)
        ){
            HashMap<String, String> tempParameters = new HashMap<>();
            String line = reader.readLine();
            while(line != null){
                if(line.contains("=")){
                    tempParameters.put(getKeyFromLine(line), getValueFromLine(line));
                }
                line = reader.readLine();
            }

            clear();
            parameters.putAll(tempParameters);
        }
    }

    /**
     * Reads the value from a string that represents a line in the configuration file.
     *
     * @param line A string that represents a line in the configuration file. The line should be on the form key=value
     * @return The value from line.
     */
    private static String getValueFromLine(String line){
        return line.substring(line.indexOf("=")+1);
    }

    /**
     * Reads the key from a string that represents a line in the configuration file.
     *
     * @param line A string that represents a line in the configuration file. The line should be on the form key=value
     * @return The key from line.
     */
    private static String getKeyFromLine(String line){
        return line.substring(0, line.indexOf("="));
    }

    /**
     * Removes all parameters from the parameters HashMap
     */
    public static void clear(){
        parameters = new HashMap<>();
    }

    /**
     * Gets the database user for this instance.
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if the return value is null
     * @return The user to be used for the database connection.
     */
    public static String getDbUser() throws NotConfiguredException {
        String user = parameters.get("username");
        verifyReturnValue(user);
        return user;
    }

    /**
     * Gets the database password for this instance.
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if the return value is null
     * @return The password to be used for the database connection.
     */
    public static String getDbPassword() throws NotConfiguredException {
        String password = parameters.get("password");
        verifyReturnValue(password);
        return password;
    }

    /**
     * Gets the host for this instance.
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if the return value is null
     * @return The host to be used for the database connection.
     */
    public static String getHost() throws NotConfiguredException {
        String host = parameters.get("host");
        verifyReturnValue(host);
        return host;
    }

    /**
     * Gets the port for this instance.
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if the return value is null
     * @return The port to be used for the database connection.
     */
    public static short getPort() throws NotConfiguredException {
        String port = parameters.get("port");
        verifyReturnValue(port);
        return new Short(port);
    }

    /**
     * Gets the database for this instance.
     *
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if the return value is null
     * @return The database to be used for the database connection.
     */
    public static String getDatabase() throws NotConfiguredException {
        String database = parameters.get("database");
        verifyReturnValue(database);
        return database;
    }

    public static boolean isConfigured(){
        return parameters.get("username") != null &&
                parameters.get("password") != null &&
                parameters.get("host") != null &&
                parameters.get("port") != null &&
                parameters.get("database") != null;
    }

    /**
     * Checks if value actually has a value, or else it throws NotConfiguredException
     * @param value the value to check not being null
     * @throws dk.tildeslash.cookbook.common.exception.NotConfiguredException if value is null
     */
    private static void verifyReturnValue(String value) throws NotConfiguredException {
        if(value == null){
            //       LOGGER.warn("value was null");
            throw new NotConfiguredException();
        }
    }
}

