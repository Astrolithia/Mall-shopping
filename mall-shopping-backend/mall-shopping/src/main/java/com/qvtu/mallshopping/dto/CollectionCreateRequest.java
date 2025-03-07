package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CollectionCreateRequest {
    private String title;
    private String handle;
    private String description;
    private String thumbnail;
    private Map<String, Object> metadata;
}
