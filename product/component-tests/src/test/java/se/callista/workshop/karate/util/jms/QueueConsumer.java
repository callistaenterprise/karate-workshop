package se.callista.workshop.karate.util.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Enumeration;

/** Utility class for consuming JMS messages using explicit fetch. */
public class QueueConsumer {

    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 5000;

    private static final Logger logger = LoggerFactory.getLogger(QueueConsumer.class);

    protected final String queueName;
    protected final boolean purge;
    protected final Connection connection;
    protected final Queue destination;
    protected final Session session;
    protected final MessageConsumer consumer;
    protected QueueBrowser browser;
    protected final long timeout;

    /**
     * Construct a queue consumer for a specific queue on a specific broker.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     */
    public QueueConsumer(String brokerUrl, String queueName) {
        this(brokerUrl, queueName, DEFAULT_TIMEOUT_MILLISECONDS, true);
    }

    /**
     * Construct a queue consumer for a specific queue on a specific broker, specifying default
     * timeout time and whether queue should be purged before starting and stopping the consumer.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     * @param timeoutInMilliseconds default timeout
     * @param purge purge the queue before starting and stopping the consumer
     */
    public QueueConsumer(
        String brokerUrl, String queueName, long timeoutInMilliseconds, boolean purge) {
        this.queueName = queueName;
        this.purge = purge;
        this.connection = ConnectionManager.getConnection(brokerUrl);
        this.timeout = timeoutInMilliseconds;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            consumer = session.createConsumer(destination);
            if (purge) {
                purgeMessages();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for and consume next message from queue. If no message arrives within the default timeout
     * interval, return null.
     *
     * @return the consumed message as a string, or null if no message available.
     */
    public Object waitForMessage() {
        return waitForMessage(timeout);
    }

    /**
     * Wait for and consume next message from queue. If no message arrives within the timeout
     * interval, return null.
     *
     * @param waitTimeout timeout interval i milliseconds
     * @return the consumed message as a string, or null if no message available.
     */
    public Object waitForMessage(long waitTimeout) {
        try {
            Message message = consumer.receive(waitTimeout);
            if (message != null) {
                if (message instanceof BytesMessage bm) {
                    byte[] resultBytes = new byte[(int)bm.getBodyLength()];
                    bm.readBytes(resultBytes);
                    return resultBytes;
                } else {
                    TextMessage tm = (TextMessage)message;
                    return tm.getText();
                }
            } else {
                return null;
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Count the messages on the queue.
     *
     * @return number of messages currently on the queue
     */
    public int size() {
        try {
            if (browser == null) {
                browser = session.createBrowser(destination);
            }
            int result = 0;
            @SuppressWarnings("unchecked")
            Enumeration<Message> messages = browser.getEnumeration();
            while (messages.hasMoreElements()) {
                messages.nextElement();
                result++;
            }
            return result;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    /** Discard any messages still on the queue. */
    public void purgeMessages() {
        try {
            while (true) {
                Message message = consumer.receive(50);
                if (message == null) {
                    logger.debug("*** no more messages to purge: {}", queueName);
                    break;
                }
                logger.info("*** purged message: {} - {}", queueName, message);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Close the consumer. */
    public void close() {
        if (purge) {
            purgeMessages();
        }
        closeConsumer(consumer);
        closeSession(session);
    }

    private void closeConsumer(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                logger.error("Exception when closing producer: ", e);
            }
        }
    }

    private void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                logger.error("Exception when closing session: ", e);
            }
        }
    }
}
