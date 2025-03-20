package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CreateDraftOrderRequest {
    private String salesChannelId;
    private String email;
    private String customerId;
    private String regionId;
    private String currencyCode;
    private List<DraftOrderShippingMethod> shippingMethods;
    private Map<String, Object> metadata;
    
    @Data
    public static class DraftOrderShippingMethod {
        private String shippingMethodId;
        private String orderId;
        private String name;
        private String optionId;
    }
} 