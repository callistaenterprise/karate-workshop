package se.callista.workshop.karate.inventory.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryValue {

    @NotNull
    @Size(max = 255)
    private String sku;

    private Long stock;
}
