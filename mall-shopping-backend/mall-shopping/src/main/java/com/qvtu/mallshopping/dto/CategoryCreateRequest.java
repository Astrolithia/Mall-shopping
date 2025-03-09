package com.qvtu.mallshopping.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CategoryCreateRequest {
    private String name;
    private String handle;
    private String description;
    private Boolean isInternal = false;
    private Boolean isActive = true;
    private Integer rank = 0;
    private Long parentCategoryId;
    private Map<String, String> metadata;
}
