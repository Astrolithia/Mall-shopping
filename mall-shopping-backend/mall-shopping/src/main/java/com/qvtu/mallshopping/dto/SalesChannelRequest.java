package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SalesChannelRequest {
    @JsonProperty("sales_channel_id")
    private String salesChannelId;
} 