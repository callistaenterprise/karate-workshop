package se.callista.workshop.karate.product.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.callista.workshop.karate.product.domain.entity.Product;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductValue {

    @JsonProperty("productId")
    private Long productId;

    @NotNull
    @Size(max = 255)
    @JsonProperty("name")
    private String name;

    @NotNull
    @Size(max = 255)
    @JsonProperty("sku")
    private String sku;

    @NotNull
    @JsonProperty("inventory")
    private Long inventory;

    public static ProductValue fromEntity(Product product, long inventory) {
        return ProductValue
            .builder()
            .productId(product.getProductId())
            .name(product.getName())
            .sku(product.getSku())
            .inventory(inventory)
            .build();
    }

    public static Product fromValue(ProductValue product) {
        return Product
            .builder()
            .productId(product.getProductId())
            .name(product.getName())
            .sku(product.getSku())
            .build();
    }
}
