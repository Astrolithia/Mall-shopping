package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LocationLevelDTO {
    @JsonProperty("inventory_item_id")
    private String inventoryItemId;
    
    @JsonProperty("location_id")
    private Long locationId;
    
    @JsonProperty("stocked_quantity")
    private Integer stockedQuantity;
    
    @JsonProperty("incoming_quantity")
    private Integer incomingQuantity;
} 