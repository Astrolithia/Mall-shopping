package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponseDTO {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String thumbnail;
    private String handle;
    private String status;

    private List<ProductOptionResponseDTO> options;
    private List<ProductVariantResponseDTO> variants;
    private Map<String, Object> metadata;

    // 基本信息
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private String createdAt;
    private String updatedAt;
}
