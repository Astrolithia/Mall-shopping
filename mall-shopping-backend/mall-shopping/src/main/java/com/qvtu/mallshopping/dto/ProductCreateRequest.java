package com.qvtu.mallshopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductCreateRequest {
    private String title;
    private String subtitle;
    private String description;
    private Boolean isGiftcard = false;
    private Boolean discountable = true;
    private String thumbnail;
    private String handle;
    private String status;

    private List<ProductOptionDTO> options;
    private List<ProductVariantDTO> variants;
    private Map<String, Object> metadata;

    // 商品尺寸和重量
    private Double weight;
    private Double length;
    private Double height;
    private Double width;

    // 其他属性
    private String originCountry;
    private String hsCode;
    private String midCode;
    private String material;
}




@Data
class PriceDTO {
    private String currency;
    private BigDecimal amount;
    private Boolean taxInclusive;
}

@Data
class InventoryItemDTO {
    private String itemId;
    private Integer quantity;
}