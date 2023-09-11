package se.callista.workshop.karate.util.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

/** Karate test container. */
public class KarateTestContainer extends GenericContainer<KarateTestContainer> {

    private static KarateTestContainer container;

    private boolean started = false;

    private KarateTestContainer(String dockerImage) {
        super(DockerImageName.parse(dockerImage));
    }

    /**
     * Get instance of Karate test container, starting it if not already started.
     *
     * @param dockerImage Docker image identifier
     * @param port exposed port
     * @param mountPath path to local folder to mount on the test container
     * @param mountName name of mounted folder in the test container
     * @param karateOpts karateOpts provided to the container command line on startup
     * @return the Karate container instance
     */
    public static KarateTestContainer getInstance(
        String dockerImage, int port, String mountPath, String mountName, String karateOpts) {
        if (container == null) {
            String cwd = DockerVolumeUtil.normalizePath(new File("").getAbsolutePath());
            container =
                new KarateTestContainer(dockerImage)
                    .withExposedPorts(port)
                    .withFileSystemBind(cwd + "/" + mountPath, "/opt/karate/" + mountName)
                    .withExtraHost("host.docker.internal", "host-gateway")
                    .withEnv("KARATE_OPTS", karateOpts);
        }
        return container;
    }

    @Override
    public void start() {
        if (!started) {
            super.start();
            System.setProperty("KARATE_HOST", container.getHost());
            System.setProperty("KARATE_PORT",
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
