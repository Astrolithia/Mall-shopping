package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String handle;
    private String description;
    private Boolean isInternal;
    private Boolean isActive;
    private Integer rank;
    private Long parent_category_id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private Map<String, String> metadata;
    private List<CategoryResponseDTO> children;
} 