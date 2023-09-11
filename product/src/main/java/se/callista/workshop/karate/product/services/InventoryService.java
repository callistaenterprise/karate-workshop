package se.callista.workshop.karate.product.services;

import se.callista.workshop.karate.inventory.model.InventoryValue;

public interface InventoryService {

    InventoryValue getInventory(String sku);
}
