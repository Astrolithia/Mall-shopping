package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UpdateOrderRequest {
    private String email;
    private String regionId;
    private String customerId;
    private String salesChannelId;
    private String currencyCode;
    private Map<String, Object> metadata;
} 