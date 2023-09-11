package se.callista.workshop.karate.util.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** ActiveMQ test container. */
public class ActiveMQTestContainer extends GenericContainer<ActiveMQTestContainer> {

    private static ActiveMQTestContainer container;

    private boolean started = false;

    private ActiveMQTestContainer(String dockerImage) {
        super(DockerImageName.parse(dockerImage));
    }

    /**
     * Get instance of Mailhog test container, starting it if not already started.
     *
     * @param dockerImage Docker image identifier
     * @param port exposed port
     * @return the ActiveMQ container instance
     */
    public static ActiveMQTestContainer getInstance(String dockerImage, int port) {
        if (container == null) {
            container =
                new ActiveMQTestContainer(dockerImage)
                    .withExposedPorts(port)
                    .withExtraHost("host.docker.internal", "host-gateway");
        }
        return container;
    }

    @Override
    public void start() {
        if (!started) {
            super.start();
            System.setProperty("ACTIVEMQ_HOST", container.getHost());
            System.setProperty("ACTIVEMQ_PORT",
                container
                    .getFirstMappedPort()
                    .toString());
            System.setProperty(
                "ACTIVEMQ_URL",
                "tcp://"
                    + System.getProperty("ACTIVEMQ_HOST")
                    + ":"
                    + System.getProperty("ACTIVEMQ_PORT"));
            started = true;
        }
    }

    @Override
    public void stop() {
        if (started) {
            super.stop();
            started = false;
        }
    }
}
