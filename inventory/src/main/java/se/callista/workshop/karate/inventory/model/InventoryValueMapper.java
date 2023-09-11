package se.callista.workshop.karate.inventory.model;

import se.callista.workshop.karate.inventory.domain.entity.Inventory;

public class InventoryValueMapper {

    private InventoryValueMapper() {
    }

    public static InventoryValue fromEntity(Inventory inventory) {
        return InventoryValue
            .builder()
            .sku(inventory.getSku())
            .stock(inventory.getStock())
            .build();
    }
}
