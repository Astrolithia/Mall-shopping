package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateInventoryItemDTO {
    private String sku;
    private Double height;
    private Double width;
    private Double length;
    private Double weight;
    @JsonProperty("mid_code")
    private String midCode;
    @JsonProperty("hs_code")
    private String hsCode;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("requires_shipping")
    private Boolean requiresShipping;
    private Map<String, Object> metadata;
} 