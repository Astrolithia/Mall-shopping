package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateInventoryItemDTO {
    private String sku;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("hs_code")
    private String hsCode;
    @JsonProperty("mid_code")
    private String midCode;
    private String material;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    @JsonProperty("requires_shipping")
    private Boolean requiresShipping;
    private Map<String, Object> metadata;
} 