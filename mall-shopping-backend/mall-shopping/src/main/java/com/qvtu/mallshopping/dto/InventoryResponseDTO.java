package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;

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
} 