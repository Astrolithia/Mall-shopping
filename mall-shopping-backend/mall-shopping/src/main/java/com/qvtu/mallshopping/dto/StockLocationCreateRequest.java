package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class StockLocationCreateRequest {
    private String name;
    private String address;
    private String city;
    private String country_code;
    private String postal_code;
    private String phone;
    private String province;
    private Map<String, Object> metadata;
} 