package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PromotionCreateRequest {
    private String code;
    private String type;
    
    @JsonProperty("is_automatic")
    private boolean isAutomatic;
    
    @JsonProperty("campaign_id")
    private String campaignId;
    
    private String status;
    private List<Map<String, Object>> rules;
    
    @JsonProperty("application_method")
    private ApplicationMethod applicationMethod;
    
    @Data
    public static class ApplicationMethod {
        private String description;
        private Double value;
        
        @JsonProperty("currency_code")
        private String currencyCode;
        
        @JsonProperty("max_quantity")
        private Integer maxQuantity;
        
        private String type;
        
        @JsonProperty("target_type")
        private String targetType;
        
        private String allocation;
        
        @JsonProperty("target_rules")
        private List<Map<String, Object>> targetRules;
        
        @JsonProperty("buy_rules")
        private List<Map<String, Object>> buyRules;
        
        @JsonProperty("apply_to_quantity")
        private Integer applyToQuantity;
        
        @JsonProperty("buy_rules_min_quantity")
        private Integer buyRulesMinQuantity;
    }
} 