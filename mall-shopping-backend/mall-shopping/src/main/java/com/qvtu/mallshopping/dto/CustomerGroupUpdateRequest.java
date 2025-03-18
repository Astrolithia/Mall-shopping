package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CustomerGroupUpdateRequest {
    private String name;
    private Map<String, Object> metadata;
} 