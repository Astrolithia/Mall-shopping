package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ShippingMethodDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal amount;
    private Boolean isTaxInclusive;
    private String shippingOptionId;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal discountTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 