package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FulfillmentProviderRequest {
    @JsonProperty("provider_id")
    private String providerId;
} 