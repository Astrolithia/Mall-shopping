package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "shipping_methods")
public class ShippingMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String name;
    private String description;
    private BigDecimal amount;
    
    @Column(name = "is_tax_inclusive")
    private Boolean isTaxInclusive;
    
    @Column(name = "shipping_option_id")
    private String shippingOptionId;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> data;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> metadata;
    
    private BigDecimal total;
    private BigDecimal subtotal;
    
    @Column(name = "tax_total")
    private BigDecimal taxTotal;
    
    @Column(name = "discount_total")
    private BigDecimal discountTotal;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 