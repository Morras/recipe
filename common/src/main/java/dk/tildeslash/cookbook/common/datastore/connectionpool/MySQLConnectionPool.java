package dk.tildeslash.cookbook.common.datastore.connectionpool;

import dk.tildeslash.cookbook.common.datastore.Configuration;
import dk.tildeslash.cookbook.common.exception.DataStoreException;
import dk.tildeslash.cookbook.common.exception.ConnectionException;
import dk.tildeslash.cookbook.common.exception.NotConfiguredException;
import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 28/07/13
 * Time: 17:22
 * To change this template use File | Settings | File Templates.
 */
public class MySQLConnectionPool {

    private static final int POOL_SIZE = 3;
    //We only ever add to the list, and only from a synchronized method
    Connection[] pooledConnections = new Connection[POOL_SIZE];
    Boolean[] inUse = new Boolean[POOL_SIZE];

    //Variables for measuring performance
    private int unPooledConnections = 0;
    private int reusedConnections = 0;

    private static MySQLConnectionPool instance;

    private String host = null;
    private short port = 0;
    private String database = null;
    private String username = null;
    private String password = null;

    private static Logger LOGGER = Logger.getLogger(MySQLConnectionPool.class);

    private MySQLConnectionPool() throws NotConfiguredException, ConnectionException {
        for ( int i = 0; i < POOL_SIZE; i++ ){
            pooledConnections[i] = null;
            inUse[i] = false;
        }
        //Let us try with the configuration already set
        //or set a new one if we catch an exception
        int tries = 0;
        while(host == null || port == 0 || database == null ||
                username == null || password == null){
            try{
                host = Configuration.getHost();
                port = Configuration.getPort();
                database = Configuration.getDatabase();
                username = Configuration.getDbUser();
                password = Configuration.getDbPassword();
            }
            catch(NotConfiguredException e){
                Configuration.load();
            }
            tries++;
            if(tries == 2){
                break;
            }
        }
        if(tries == 3){
            throw new NotConfiguredException("Unable to load configuration from file");
        }
        //Not sure what to do with these exceptions
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
            LOGGER.error("Exceptions while setting up the connector: " + e.getMessage());
            throw new ConnectionException("Unable to connect to the database", e);
        }

    }

    public static MySQLConnectionPool getInstance() throws ConnectionException, DataStoreException, NotConfiguredException {
        if ( instance == null ){
            instance = new MySQLConnectionPool();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws ConnectionException{
        Connection connection;
        for ( int i = 0; i < POOL_SIZE; i++ ){
            if ( pooledConnections[i] == null ){
                connection = createConnection();
                PooledSqlConnection pooledConnection = new PooledSqlConnection(this, connection, true);
                setInUse(i);
                pooledConnections[i] = pooledConnection;
                return pooledConnection;
            } else if ( ! inUse[i] ){
                connection = pooledConnections[i];
                setInUse(i);

                try{
                    if ( ! connection.isValid(3) ){
                        LOGGER.warn("A connection is no longer valid.");
                        connection = createConnection();
                    } else {
                        reusedConnections++;
                        LOGGER.info("Reusing an established connecting, this is the " + reusedConnections + " time a connection is reused.");
                    }
                } catch (SQLException e){
                    throw new ConnectionException("Unable to determine if the old exception is valid: " + e.getMessage());
                }

                return connection;
            }
        }

        unPooledConnections++;
        connection = createConnection();
        return new PooledSqlConnection(this, connection, false);
    }

    private Connection createConnection() throws ConnectionException{
        try{
            LOGGER.info("Creating a connection, there have been a total of " + unPooledConnections + " unpooled connections.");
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" +
                    port + "/" + database + "?user=" +
                    username + "&password=" + password +
                    "&default-character-set=utf8");
        } catch (SQLException e){
            LOGGER.error("Unable to establish connection. " + e.getMessage());
            throw new ConnectionException("Unable to establish connection", e);
        }
    }

    private void setInUse(int connectionNumber){
        inUse[connectionNumber] = true;
    }

    void release(PooledSqlConnection connection) throws DataStoreException{
        for ( int i = 0; i < POOL_SIZE; i++){
            if ( pooledConnections[i] == connection ){
                try{
                    if ( ! connection.getAutoCommit() ){
                        connection.rollback(); //It is the responsibility of the client to make sure all work is committed
                    }
                    connection.setAutoCommit(true);
                } catch (SQLException e){
                    LOGGER.error("Exception while cleaning up connection: " + e.getMessage());
                    try{
                        connection.destroyConnection();
                        inUse[i] = false;
                        pooledConnections[i] = null;
                    } catch(SQLException ex){
                        LOGGER.error("Unable to close connection: " + ex.getMessage());
                        throw new DataStoreException("Unable to close connection", ex);
                    }
                }
                inUse[i] = false;
                return;
            }
        }
    }
}
