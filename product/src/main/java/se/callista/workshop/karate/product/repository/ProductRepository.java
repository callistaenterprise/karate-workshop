package se.callista.workshop.karate.product.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import se.callista.workshop.karate.product.domain.entity.Product;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {

    @Transactional(readOnly = true)
    Optional<Product> findByProductId(Long id);

    @Transactional(readOnly = true)
    Optional<Product> findBySku(String sku);
}
