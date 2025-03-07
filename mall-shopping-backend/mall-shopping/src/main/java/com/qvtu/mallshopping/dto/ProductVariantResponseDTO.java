package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariantResponseDTO {
    private Long id;
    private String title;
    private String sku;
    private List<PriceResponseDTO> prices;
}
