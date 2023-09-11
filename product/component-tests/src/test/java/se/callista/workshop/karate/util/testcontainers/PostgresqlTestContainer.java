package se.callista.workshop.karate.util.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** Mailhog test container. */
public class PostgresqlTestContainer extends GenericContainer<PostgresqlTestContainer> {

    private static PostgresqlTestContainer container;

    private final int port;

    private boolean started = false;

    private PostgresqlTestContainer(String dockerImage, int port) {
        super(DockerImageName.parse(dockerImage));
        this.port = port;
    }

    /**
     * Get instance of PostgreSQL test container, starting it if not already started.
     *
     * @param dockerImage Docker image identifier
     * @param port exposed port
     * @param database database name
     * @return the PostgreSQL container instance
     */
    public static PostgresqlTestContainer getInstance(
        String dockerImage, int port, String user, String password, String database) {
        if (container == null) {
            container =
                new PostgresqlTestContainer(dockerImage, port)
                    .withExposedPorts(port)
                    .withEnv("POSTGRES_USER", user)
                    .withEnv("POSTGRES_PASSWORD", password)
                    .withEnv("POSTGRES_DB", database)
                    .withExtraHost("host.docker.internal", "host-gateway");
        }
        return container;
    }

    @Override
    public void start() {
        if (!started) {
            super.start();
            System.setProperty("POSTGRESQL_HOST", container.getHost());
            System.setProperty("POSTGRESQL_PORT",
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
