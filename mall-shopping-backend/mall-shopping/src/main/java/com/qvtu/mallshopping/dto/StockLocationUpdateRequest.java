package com.qvtu.mallshopping.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@Data
public class StockLocationUpdateRequest {
    @JsonProperty("address_id")
    private String addressId;
    private Map<String, Object> metadata;
} 