package se.callista.workshop.karate.util.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

/** Utility class for managing ActiveMQ connections. */
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private static final Map<String, Connection> connectionCache = new HashMap<>();

    private ConnectionManager() {
    }

    public static Connection getConnection(String brokerUrl) {
        Connection connection = connectionCache.get(brokerUrl);
        if (connection == null) {
            try {
                logger.debug("waiting for ActiveMQ connection ...");
                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
                connection = connectionFactory.createConnection();
                connection.start();
                connectionCache.put(brokerUrl, connection);
                logger.debug("ActiveMQ connection established");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public static void closeConnection(String brokerUrl) {
        Connection connection = connectionCache.get(brokerUrl);
        if (connection != null) {
            try {
                connection.stop();
                connection.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                connectionCache.remove(brokerUrl);
            }
        }
    }
}
