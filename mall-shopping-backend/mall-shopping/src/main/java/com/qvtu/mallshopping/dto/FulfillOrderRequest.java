package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FulfillOrderRequest {
    private List<FulfillmentItem> items;
    
    @JsonProperty("location_id")
    private String locationId;
    
    private Map<String, Object> metadata;
    
    @Data
    public static class FulfillmentItem {
        private String id;
        private Integer quantity;
    }
} 