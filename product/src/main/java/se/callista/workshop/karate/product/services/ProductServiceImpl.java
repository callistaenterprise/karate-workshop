package se.callista.workshop.karate.product.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import se.callista.workshop.karate.inventory.model.InventoryValue;
import se.callista.workshop.karate.product.domain.entity.Product;
import se.callista.workshop.karate.product.model.ProductValue;
import se.callista.workshop.karate.product.repository.ProductRepository;

@RequiredArgsConstructor
@Component
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final JmsTemplate jmsTemplate;

    @Override
    public ProductValue getProduct(String sku) {
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new EntityNotFoundException("Product " + sku + " not found"));
        long stock;
        try {
            InventoryValue inventoryValue = inventoryService.getInventory(product.getSku());
            stock = inventoryValue.getStock();
        } catch (RuntimeException e) {
            stock = 0;
        }
        return ProductValue.fromEntity(product, stock);
    }

    @Override
    public ProductValue createProduct(ProductValue productValue) {
        Product product = ProductValue.fromValue(productValue);
        try {
            productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateKeyException(productValue.getSku() + " already exists");
        }
        InventoryValue inventoryValue =
            InventoryValue
                .builder()
                .sku(product.getSku())
                .stock(0L)
                .build();
        jmsTemplate.convertAndSend("replenish", inventoryValue);
        return ProductValue.fromEntity(product, 0L);
    }

    @Override
    public void deleteProduct(final String sku) {
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new EntityNotFoundException("Product " + sku + " not found"));
        productRepository.delete(product);
        InventoryValue inventoryValue =
            InventoryValue
                .builder()
                .sku(product.getSku())
                .stock(-1L)
                .build();
        jmsTemplate.convertAndSend("replenish", inventoryValue);
    }
}
