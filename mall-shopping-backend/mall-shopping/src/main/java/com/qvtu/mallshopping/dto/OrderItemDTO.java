package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class OrderItemDTO {
    private String id;
    private String title;
    private String subtitle;
    private String thumbnail;
    private String variantId;
    private String productId;
    private String productTitle;
    private String productDescription;
    private String variantTitle;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Boolean requiresShipping;
    private Boolean isDiscountable;
    private Boolean isTaxInclusive;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal discountTotal;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 