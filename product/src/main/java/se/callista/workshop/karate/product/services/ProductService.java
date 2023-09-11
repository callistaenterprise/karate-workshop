package se.callista.workshop.karate.product.services;

import se.callista.workshop.karate.product.model.ProductValue;

public interface ProductService {

    ProductValue getProduct(String sku);

    ProductValue createProduct(ProductValue product);

    void deleteProduct(String sku);

}
