package com.qvtu.mallshopping.dto;

import com.qvtu.mallshopping.model.Product;
import lombok.Data;

@Data
public class SimpleProductDTO {
    private Long id;
    private String title;
    private String handle;
    private String thumbnail;
    
    // 从 Product 实体创建 DTO
    public static SimpleProductDTO fromProduct(Product product) {
        SimpleProductDTO dto = new SimpleProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setHandle(product.getHandle());
        dto.setThumbnail(product.getThumbnail());
        return dto;
    }
} 