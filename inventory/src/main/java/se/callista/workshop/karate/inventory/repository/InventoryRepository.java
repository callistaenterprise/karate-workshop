package se.callista.workshop.karate.inventory.repository;

import org.springframework.data.repository.CrudRepository;
import se.callista.workshop.karate.inventory.domain.entity.Inventory;

import java.util.Optional;

public interface InventoryRepository extends CrudRepository<Inventory, String> {

    Optional<Inventory> findBySku(String sku);
}
