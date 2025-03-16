package com.qvtu.mallshopping.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@Data
public class ReservationCreateRequest {
    @JsonProperty("line_item_id")
    private String lineItemId;
    
    @JsonProperty("inventory_item_id")
    private String inventoryItemId;
    
    @JsonProperty("location_id") 
    private String locationId;
    
    private Integer quantity;
    
    private String description;
    
    @JsonProperty("external_id")
    private String externalId;
    
    private Map<String, Object> metadata;
} 