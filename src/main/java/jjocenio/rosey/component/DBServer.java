package jjocenio.rosey.component;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

import javax.persistence.PersistenceException;

import static org.hsqldb.Database.CLOSEMODE_NORMAL;

public class DBServer {

    private static final String DATABASE_PATH_PATTERN = "file:/%s/rosey/db";

    private final Server hsqlServer;

    public DBServer() throws PersistenceException {
        HsqlProperties hsqlProperties = new HsqlProperties();
        hsqlProperties.setProperty("server.database.0", getDatabasePath());
        hsqlProperties.setProperty("server.dbname.0", "rosey");
        hsqlProperties.setProperty("server.remote_open", true);

        this.hsqlServer = new Server();
        this.hsqlServer.setTrace(false);
        this.hsqlServer.setSilent(true);
        this.hsqlServer.setLogWriter(null);
        this.hsqlServer.setErrWriter(null);

        try {
            this.hsqlServer.setProperties(hsqlProperties);
        } catch (Exception e) {
            throw new PersistenceException("Error setting database configuration.", e);
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownDBServerHook(this));
    }

    public void start() {
        this.hsqlServer.start();
    }

    public void stop() {
        this.hsqlServer.shutdownCatalogs(CLOSEMODE_NORMAL);
        this.hsqlServer.stop();
    }

    private String getDatabasePath() {
        return String.format(DATABASE_PATH_PATTERN, System.getProperty("user.home"));
    }
}

class ShutdownDBServerHook extends Thread {

    private final DBServer dbServer;

    ShutdownDBServerHook(DBServer dbServer) {
        this.dbServer = dbServer;
    }

    @Override
    public void run() {
        this.dbServer.stop();
    }
}
