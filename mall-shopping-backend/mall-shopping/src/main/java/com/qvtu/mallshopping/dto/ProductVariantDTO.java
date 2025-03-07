package com.qvtu.mallshopping.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductVariantDTO {
    private String title;
    private String sku;
    private Boolean managedInventory;
    private Boolean allowBackorder;
    private Boolean hasInventoryKit;
    private List<PriceDTO> prices;
    private List<InventoryItemDTO> inventoryItems;
}
