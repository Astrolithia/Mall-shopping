package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class Budget {
    private String id;
    private String type;
    private String currencyCode;
    private Double limit;
    private Double used;
} 