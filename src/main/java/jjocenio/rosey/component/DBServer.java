package jjocenio.rosey.component;

import jjocenio.rosey.ApplicationExitRequestEvent;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.springframework.context.event.EventListener;

import javax.persistence.PersistenceException;
import java.io.File;

import static org.hsqldb.Database.CLOSEMODE_NORMAL;

public class DBServer {

    private final Server hsqlServer;

    public DBServer(File workingDirectory) throws PersistenceException {
        HsqlProperties hsqlProperties = new HsqlProperties();
        hsqlProperties.setProperty("server.database.0", workingDirectory.getPath() + File.separator + "db" + File.separator + "rosey");
        hsqlProperties.setProperty("server.dbname.0", "rosey");
        hsqlProperties.setProperty("server.remote_open", true);

        this.hsqlServer = new Server();
        this.hsqlServer.setSilent(true);
        this.hsqlServer.setTrace(false);
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

    @EventListener
    public void handleContextStoppedEvent(ApplicationExitRequestEvent event) {
        this.stop();
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
