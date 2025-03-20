package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String title;
    private String subtitle;
    private String thumbnail;
    
    @Column(name = "variant_id")
    private String variantId;
    
    @Column(name = "product_id")
    private String productId;
    
    @Column(name = "product_title")
    private String productTitle;
    
    @Column(name = "product_description")
    private String productDescription;
    
    @Column(name = "variant_title")
    private String variantTitle;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    private Integer quantity;
    
    @Column(name = "requires_shipping")
    private Boolean requiresShipping;
    
    @Column(name = "is_discountable")
    private Boolean isDiscountable;
    
    @Column(name = "is_tax_inclusive")
    private Boolean isTaxInclusive;
    
    private BigDecimal total;
    private BigDecimal subtotal;
    
    @Column(name = "tax_total")
    private BigDecimal taxTotal;
    
    @Column(name = "discount_total")
    private BigDecimal discountTotal;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 