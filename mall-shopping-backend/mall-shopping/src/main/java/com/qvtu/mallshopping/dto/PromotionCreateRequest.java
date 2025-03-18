package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PromotionCreateRequest {
    private String code;
    private String type;
    private boolean isAutomatic;
    private String campaignId;
    private String status;
    private List<Map<String, Object>> rules;
    private ApplicationMethod applicationMethod;
    
    @Data
    public static class ApplicationMethod {
        private String description;
        private Double value;
        private String currencyCode;
        private Integer maxQuantity;
        private String type;
        private String targetType;
        private String allocation;
        private List<Map<String, Object>> targetRules;
        private List<Map<String, Object>> buyRules;
        private Integer applyToQuantity;
        private Integer buyRulesMinQuantity;
    }
} 