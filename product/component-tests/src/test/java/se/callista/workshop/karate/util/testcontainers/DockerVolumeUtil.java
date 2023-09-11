package se.callista.workshop.karate.util.testcontainers;

class DockerVolumeUtil {

    private DockerVolumeUtil() {
    }

    static String normalizePath(String path) {
        String result = path;
        if (path.matches("^([A-Za-z]):.*")) {
            String drive = path.substring(0, 1);
            result = "/mnt/" + drive.toLowerCase() + path.substring(2);
        }
        return result.replace('\\', '/');
    }
}
