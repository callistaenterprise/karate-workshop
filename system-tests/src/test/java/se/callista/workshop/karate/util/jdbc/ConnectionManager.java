package se.callista.workshop.karate.util.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/** Utility class for managing PostgreSQL connections. */
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private static final Map<String, Connection> connectionCache = new HashMap<>();

    private ConnectionManager() {
    }

    public static Connection getConnection(String url, String user, String password) {
        Connection connection = connectionCache.get(url);
        if (connection == null) {
            try {
                logger.debug("waiting for PostgreSQL connection ...");
                connection = DriverManager.getConnection(url, user, password);
                connectionCache.put(url, connection);
                logger.debug("PostgreSQL connection established");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public static void closeConnection(String url) {
        Connection connection = connectionCache.get(url);
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                connectionCache.remove(url);
            }
        }
    }
}
