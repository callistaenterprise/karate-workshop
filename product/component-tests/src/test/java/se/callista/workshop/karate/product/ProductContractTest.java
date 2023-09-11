package se.callista.workshop.karate.product;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import se.callista.workshop.karate.util.testcontainers.ActiveMQTestContainer;
import se.callista.workshop.karate.util.testcontainers.KarateTestContainer;
import se.callista.workshop.karate.util.testcontainers.PostgresqlTestContainer;
import se.callista.workshop.karate.util.testcontainers.SystemUnderTestContainer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductContractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductContractTest.class);

    @BeforeAll
    static void startContainers() {
        activemq.start();
        postgresql.start();
        inventoryMock.start();
        product.start();
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
        inventoryMock.followOutput(logConsumer);
    }

    @AfterAll
    static void stopContainers() {
        product.stop();
        inventoryMock.stop();
        postgresql.stop();
        activemq.stop();
    }

    @Container
    private static final PostgresqlTestContainer postgresql =
        PostgresqlTestContainer.getInstance("postgres:latest", 5432, "admin", "secret", "product");

    @Container
    private static final ActiveMQTestContainer activemq =
        ActiveMQTestContainer.getInstance("symptoma/activemq:latest", 61616);

    @Container
    private static final KarateTestContainer inventoryMock =
        KarateTestContainer.getInstance(
            "karate:1.4.0",
            9443,
            "src/test/java/se/callista/workshop/karate/inventory",
            "inventory",
            "-m ./inventory/InventoryMock.feature -p 9443 -s -c ./certs/server.crt -k ./certs/server.key");

    @Container
    private static final SystemUnderTestContainer product =
        SystemUnderTestContainer
            .getInstance(
                "product:" + System.getProperty("product.version", "1.0.0"),
                8443,
                Map.of(
                    "SPRING_ACTIVEMQ_BROKER_URL", "tcp://host.docker.internal:${ACTIVEMQ_PORT}",
                    "SPRING_DATASOURCE_URL",
                    "jdbc:postgresql://host.docker.internal:${POSTGRESQL_PORT}/product",
                    "INVENTORY_URL", "https://host.docker.internal:${KARATE_PORT}/inventory/"))
            .dependsOn(activemq)
            .dependsOn(postgresql)
            .dependsOn(inventoryMock);

    @Test
    void testProduct() {
        Results results = Runner
            .path("classpath:se/callista/workshop/karate/product")
            .parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

}
