package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class InventoryResponseDTO {
    private String id;
    private String sku;
    private Integer quantity;
    private Boolean allowBackorder;
    private Boolean manageInventory;
    private Boolean requiresShipping;
    private LocationResponseDTO location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Map<String, Object> metadata;
} 