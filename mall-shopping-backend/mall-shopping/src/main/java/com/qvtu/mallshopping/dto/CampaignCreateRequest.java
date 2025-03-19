package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CampaignCreateRequest {
    private String name;
    private String campaignIdentifier;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Budget budget;
} 