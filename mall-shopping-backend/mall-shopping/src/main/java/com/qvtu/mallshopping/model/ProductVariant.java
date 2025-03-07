package com.qvtu.mallshopping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String sku;

    private Boolean managedInventory = true;

    private Boolean allowBackorder = false;

    private Boolean hasInventoryKit = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
