package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateOrderRequest {
    private String email;
    
    @JsonProperty("region_id")
    private String regionId;
    
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("sales_channel_id")
    private String salesChannelId;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Map<String, Object> metadata;
    private String status;
    
    @JsonProperty("payment_status")
    private String paymentStatus;
    
    @JsonProperty("fulfillment_status")
    private String fulfillmentStatus;
    
    @Override
    public String toString() {
        return "UpdateOrderRequest{" +
            "paymentStatus='" + paymentStatus + '\'' +
            ", fulfillmentStatus='" + fulfillmentStatus + '\'' +
            '}';
    }
} 