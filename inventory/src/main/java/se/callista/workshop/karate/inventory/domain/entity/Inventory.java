package se.callista.workshop.karate.inventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @Column(name = "sku", unique = true, nullable = false, updatable = false)
    protected String sku;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "int default 0")
    protected Integer version;

    @Column(name = "stock", nullable = false)
    @NotNull
    private Long stock;

    @Builder
    public Inventory(String sku, Long stock, Integer version) {
        this.sku = sku;
        this.stock = stock;
        this.version = version;
    }
}
