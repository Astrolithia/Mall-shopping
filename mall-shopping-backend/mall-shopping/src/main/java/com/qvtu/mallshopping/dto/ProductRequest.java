package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ProductRequest {
    private String title;
    private String handle;
    private String description;
    private String thumbnail;
    private Boolean isGiftcard;
    private Boolean discountable;
    private String subtitle;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private String originCountry;
    private String hsCode;
    private String midCode;
    private String material;
    private Map<String, Object> metadata;
}
