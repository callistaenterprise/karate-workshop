package se.callista.workshop.karate.util.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Utility class for consuming JMS messages using a message listener. Incoming messages are consumed
 * and stored in-memory when they arrive, available for later retrieval.
 */
public class QueueListener extends QueueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(QueueConsumer.class);

    private final List<Object> messages = new ArrayList<>();

    // Semaphore used when waiting for messages
    private CompletableFuture<Void> waitSemaphore;
    // Condition used when waiting for messages
    private Predicate<Object> waitCondition;

    /**
     * Construct a queue listener for a specific queue on a specific broker.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     */
    public QueueListener(String brokerUrl, String queueName) {
        this(brokerUrl, queueName, DEFAULT_TIMEOUT_MILLISECONDS, true);
    }

    /**
     * Construct a queue listener for a specific queue on a specific broker, specifying default
     * timeout time and whether queue should be purged before starting and stopping the consumer.
     *
     * @param brokerUrl ActiveMQ broker url
     * @param queueName Queue name
     * @param timeoutInMilliseconds default timeout
     * @param purge purge the queue before starting and stopping the listener
     */
    public QueueListener(
        String brokerUrl, String queueName, long timeoutInMilliseconds, boolean purge) {
        super(brokerUrl, queueName, timeoutInMilliseconds, purge);
        try {
            consumer.setMessageListener(
                message -> {
                    try {
                        if (message instanceof BytesMessage bm) {
                            byte[] resultBytes = new byte[(int)bm.getBodyLength()];
                            bm.readBytes(resultBytes);
                            append(resultBytes);
                        } else {
                            TextMessage tm = (TextMessage)message;
                            append(tm.getText());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all messages.
     *
     * @return List of strings containing all messages consumed.
     */
    public List<Object> collect() {
        synchronized (messages) {
            return messages;
        }
    }

    /**
     * Return the number of messages consumed and available.
     *
     * @return number of messages.
     */
    @Override
    public int size() {
        synchronized (messages) {
            return messages.size();
        }
    }

    /**
     * Get message at specific index (starting from 0).
     *
     * @param index the message index
     * @return the message as a String
     */
    public Object get(int index) {
        synchronized (messages) {
            return messages.get(index);
        }
    }

    /**
     * Discard all messages consumed.
     *
     * @return Number of messages consumed and discarded.
     */
    public int clear() {
        synchronized (messages) {
            int size = messages.size();
            messages.clear();
            return size;
        }
    }

    /**
     * Wait until a specified number of messages have arrived. Wait at most up until default timeout
     * interval.
     *
     * @param count the expected number of messages.
     * @return List of strings containing all messages consumed.
     */
    public List<Object> waitUntilCount(int count) {
        return waitUntilCount(count, this.timeout);
    }

    /**
     * Wait until a specified number of messages have arrived, or a specified timeout interval has
     * passed.
     *
     * @param count the expected number of messages.
     * @param waitTimeout wait timeout interval in milliseconds.
     * @return List of strings containing all messages consumed.
     */
    public List<Object> waitUntilCount(int count, long waitTimeout) {
        if (size() < count) {
            waitSemaphore = new CompletableFuture<>();
            waitCondition = o -> size() >= count;
            try {
                waitSemaphore.get(waitTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("wait timed out: {}", e + "");
            } finally {
                waitCondition = null;
                waitSemaphore = null;
            }
        }
        return messages;
    }

    @Override
    public Object waitForMessage(long waitTimeout) {
        if (size() < 1) {
            waitSemaphore = new CompletableFuture<>();
            waitCondition = o -> size() == 1;
            try {
                waitSemaphore.get(waitTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("wait timed out: {}", e + "");
            } finally {
                waitCondition = null;
                waitSemaphore = null;
            }
        }
        return size() > 0 ? get(0) : null;
    }

    /** Close the listener. */
    public void close() {
        try {
            consumer.setMessageListener(null);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        super.close();
        clear();
    }

    // Append a message to storage, and check whether there is an active
    // wait for messages. If the wait condition is met, raise the semaphore
    // by completing it.
    private void append(Object message) {
        synchronized (messages) {
            messages.add(message);
        }
        if (waitCondition != null && waitCondition.test(message)) {
            logger.debug("condition met, will signal completion");
            waitSemaphore.complete(null);
        } else {
            logger.debug("condition not met, will continue waiting");
        }
    }
}
