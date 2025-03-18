package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class StockLocationResponseDTO {
    private String id;
    private String name;
    private String address_id;  // 我们可以用地址字段的ID作为address_id
} 