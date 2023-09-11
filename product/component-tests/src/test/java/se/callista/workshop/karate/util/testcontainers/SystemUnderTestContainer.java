package se.callista.workshop.karate.util.testcontainers;

import org.apache.commons.text.StringSubstitutor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/** Mule-ee test container. */
public class SystemUnderTestContainer extends GenericContainer<SystemUnderTestContainer> {

    private static SystemUnderTestContainer container;
    private boolean started = false;
    private Map<String, String> environment = null;

    private SystemUnderTestContainer(String dockerImage) {
        super(DockerImageName.parse(dockerImage));
    }

    /**
     * Get instance of test container, starting it if not already started.
     *
     * @param dockerImage Docker image identifier
     * @param port exposed port
     * @param environment environment variables provided to the container on startup
     * @return the container instance
     */
    public static SystemUnderTestContainer getInstance(
        String dockerImage, int port, Map<String, String> environment) {
        if (container == null) {
            container =
                new SystemUnderTestContainer(dockerImage)
                    .withExposedPorts(port)
                    .withExtraHost("host.docker.internal", "host-gateway");
            container.environment = environment;
        }
        return container;
    }

    @Override
    public void start() {
        if (!started) {
            for (Map.Entry<String, String> environmentEntry : environment.entrySet()) {
                container.addEnv(
                    environmentEntry.getKey(),
                    StringSubstitutor.replaceSystemProperties(environmentEntry.getValue()));
            }
            super.start();
            System.setProperty("SYSTEM_UNDER_TEST_HOST", container.getHost());
            System.setProperty("SYSTEM_UNDER_TEST_PORT",
                container
                    .getFirstMappedPort()
                    .toString());
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
