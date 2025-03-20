package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private String id;
    private BigDecimal amount;
    private String currencyCode;
    private String providerId;
} 