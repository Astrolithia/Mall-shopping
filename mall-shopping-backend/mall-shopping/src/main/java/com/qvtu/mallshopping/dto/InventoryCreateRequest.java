package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class InventoryCreateRequest {
    private String sku;
    private Integer quantity;
    private Boolean allowBackorder;
    private Boolean manageInventory;
    private Long locationId;
} 