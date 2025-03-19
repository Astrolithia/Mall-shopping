package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CampaignResponse {
    private String id;
    private String name;
    private String description;
    private String currency;
    private String campaignIdentifier;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Budget budget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
} 