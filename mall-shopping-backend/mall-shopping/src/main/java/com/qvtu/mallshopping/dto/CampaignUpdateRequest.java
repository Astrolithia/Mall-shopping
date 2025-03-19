package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CampaignUpdateRequest {
    private String name;
    private String description;
    private String campaignIdentifier;
    private String currency;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Budget budget;
} 