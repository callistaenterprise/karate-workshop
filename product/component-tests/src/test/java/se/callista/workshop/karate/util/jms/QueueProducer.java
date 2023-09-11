package se.callista.workshop.karate.util.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/** Utility class for producing JMS messages. */
public class QueueProducer {

    private static final Logger logger = LoggerFactory.getLogger(QueueProducer.class);

    protected final String queueName;
    protected final boolean purge;
    protected final Connection connection;
    protected final Session session;
    protected final Queue destination;
    protected final MessageProducer producer;
    protected MessageConsumer consumer;
    protected QueueBrowser browser;

    /**
     * Construct a queue consumer for a specific queue on a specific broker.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     */
    public QueueProducer(String brokerUrl, String queueName) {
        this(brokerUrl, queueName, true);
    }

    /**
     * Construct a queue consumer for a specific queue on a specific broker, specifying whether queue
     * should be purged before starting and stopping the producer.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     * @param purge purge the queue before starting and stopping the producer
     */
    public QueueProducer(String brokerUrl, String queueName, boolean purge) {
        this.queueName = queueName;
        this.purge = purge;
        this.connection = ConnectionManager.getConnection(brokerUrl);
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            if (purge) {
                purgeMessages();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a message.
     *
     * @param message The message, either as a String or as a Byte Array
     */
    public void send(Object message) {
        send(message, null, null, null, null);
    }

    /**
     * Send a message with custom properties.
     *
     * @param message The message, either as a String or as a Byte Array
     * @param properties message properties
     */
    public void send(Object message, Map<String, String> properties) {
        send(message, properties, null, null, null);
    }

    /**
     * Send a message with custom properties, correlationId, replyTo and delay.
     *
     * @param message The message, either as a String or as a Byte Array
     * @param properties message properties
     * @param correlationId Correlation id message header
     * @param replyTo replyTo message header
     * @param delayMillis Delay in milliseconds before sending the message
     */
    public void send(
        Object message,
        Map<String, String> properties,
        String correlationId,
        String replyTo,
        Integer delayMillis) {
        try {
            if (delayMillis != null && delayMillis > 0) {
                logger.info("*** scheduled delay: {}", delayMillis);
                Thread.sleep(delayMillis);
            }
            Message jmsMessage;
            if (message instanceof byte[]) {
                jmsMessage = session.createBytesMessage();
                ((BytesMessage)jmsMessage).writeBytes((byte[])message);
            } else {
                jmsMessage = session.createTextMessage(message.toString());
            }
            if (properties != null) {
                for (Map.Entry<String, String> property : properties.entrySet()) {
                    jmsMessage.setStringProperty(property.getKey(), property.getValue());
                }
            }
            if (correlationId != null) {
                jmsMessage.setJMSCorrelationID(correlationId);
            }
            if (replyTo != null) {
                Destination replyToDestination = session.createQueue(replyTo);
                jmsMessage.setJMSReplyTo(replyToDestination);
            }
            producer.send(jmsMessage);
            if (message instanceof byte[]) {
                logger.info(
                    "*** sent byte[] message: {}", new String((byte[])message, StandardCharsets.UTF_8));
            } else {
                logger.info("*** sent message: {}", message);
            }
        } catch (Exception e) {
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
            if (consumer == null) {
                consumer = session.createConsumer(destination);
            }
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

    /** Close the producer. */
    public void close() {
        if (purge) {
            purgeMessages();
        }
        closeProducer(producer);
        closeConsumer(consumer);
        closeBrowser(browser);
        closeSession(session);
    }

    private void closeProducer(MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                logger.error("Exception when closing producer: ", e);
            }
        }
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

    private void closeBrowser(QueueBrowser browser) {
        if (browser != null) {
            try {
                browser.close();
            } catch (JMSException e) {
                logger.error("Exception when closing browser: ", e);
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
