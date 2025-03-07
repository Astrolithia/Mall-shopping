package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String title;
    private String subtitle;
    private String description;
    private String handle;
    private String status;
}
