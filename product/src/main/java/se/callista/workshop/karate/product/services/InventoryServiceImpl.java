package se.callista.workshop.karate.product.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import se.callista.workshop.karate.inventory.model.InventoryValue;

import java.util.concurrent.Semaphore;

@Component
public class InventoryServiceImpl implements InventoryService {

    private final Semaphore INVENTORY_SEMAPHORE;

    private final WebClient.Builder webClientBuilder;

    @Value("${inventory.url}")
    private String url;

    public InventoryServiceImpl(
        @Value("${inventory.session.max:1000}") int permits, WebClient.Builder webClientBuilder) {
        INVENTORY_SEMAPHORE = new Semaphore(permits);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public InventoryValue getInventory(String sku) {
        try {
            INVENTORY_SEMAPHORE.acquire();
            return webClientBuilder
                .build()
                .get()
                .uri(url + sku)
                .retrieve()
                .bodyToMono(InventoryValue.class)
                .block();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            INVENTORY_SEMAPHORE.release();
        }
    }
}
