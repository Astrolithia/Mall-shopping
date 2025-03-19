package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PromotionUpdateRequest {
    private String code;
    private String type;
    
    @JsonProperty("is_automatic")
    private Boolean isAutomatic;
    
    @JsonProperty("campaign_id")
    private String campaignId;
    
    private String status;
    private List<Map<String, Object>> rules;
    
    @JsonProperty("application_method")
    private PromotionCreateRequest.ApplicationMethod applicationMethod;
} 