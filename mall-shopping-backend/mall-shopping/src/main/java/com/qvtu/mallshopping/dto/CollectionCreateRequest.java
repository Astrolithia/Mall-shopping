package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class CollectionCreateRequest {
    private String title;
    private String handle;
    private String description;
    private String thumbnail;
    private String metadata;
}
