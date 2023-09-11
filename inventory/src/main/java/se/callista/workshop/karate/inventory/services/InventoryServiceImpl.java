package se.callista.workshop.karate.inventory.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.callista.workshop.karate.inventory.domain.entity.Inventory;
import se.callista.workshop.karate.inventory.model.InventoryValue;
import se.callista.workshop.karate.inventory.model.InventoryValueMapper;
import se.callista.workshop.karate.inventory.repository.InventoryRepository;

@Component
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final JmsTemplate jmsTemplate;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, JmsTemplate jmsTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryValue getInventory(String sku) {
        return inventoryRepository
            .findBySku(sku)
            .map(InventoryValueMapper::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Inventory with sku " + sku + " not found"));
    }

    @JmsListener(destination = "replenish", containerFactory = "containerFactory")
    public void receiveReplenishMessage(InventoryValue inventoryValue) {
        Inventory inventory =
            inventoryRepository
                .findBySku(inventoryValue.getSku())
                .orElse(Inventory
                    .builder()
                    .sku(inventoryValue.getSku())
                    .stock(0L)
                    .build());
        if (inventoryValue.getStock() < 0) {
            inventoryRepository.delete(inventory);
        } else {
            inventory.setStock(inventory.getStock() + inventoryValue.getStock());
            inventoryRepository.save(inventory);
            jmsTemplate.convertAndSend("stocklevel", InventoryValueMapper.fromEntity(inventory));
        }
    }
}
