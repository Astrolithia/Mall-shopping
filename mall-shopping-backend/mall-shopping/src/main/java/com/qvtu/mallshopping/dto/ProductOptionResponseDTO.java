package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductOptionResponseDTO {
    private Long id;
    private String title;
    private List<String> values;
}
