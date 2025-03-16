package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ReservationUpdateRequest {
    private String description;
    private Map<String, Object> metadata;
} 