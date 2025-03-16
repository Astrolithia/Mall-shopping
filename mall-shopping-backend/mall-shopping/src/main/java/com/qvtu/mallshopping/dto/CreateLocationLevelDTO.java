package com.qvtu.mallshopping.dto;

import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;

@Data
@ToString
public class CreateLocationLevelDTO {
    @JsonProperty("location_id")
    private String locationId;
    
    @JsonProperty("stocked_quantity")
    private Integer stockedQuantity = 0;
    
    @JsonProperty("incoming_quantity")
    private Integer incomingQuantity = 0;
    
    private Map<String, Object> metadata = new HashMap<>();

    // 添加验证方法
    public void validate() {
        if (locationId == null || locationId.trim().isEmpty()) {
            throw new IllegalArgumentException("location_id is required");
        }
        if (stockedQuantity == null) {
            stockedQuantity = 0;
        }
        if (incomingQuantity == null) {
            incomingQuantity = 0;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }
} 