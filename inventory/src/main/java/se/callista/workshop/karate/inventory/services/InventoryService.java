package se.callista.workshop.karate.inventory.services;

import se.callista.workshop.karate.inventory.model.InventoryValue;

public interface InventoryService {

    InventoryValue getInventory(String sku);
}
