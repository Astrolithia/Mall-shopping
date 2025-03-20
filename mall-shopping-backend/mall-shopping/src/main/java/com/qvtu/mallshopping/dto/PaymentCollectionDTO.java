package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentCollectionDTO {
    private String id;
    private String currencyCode;
    private BigDecimal amount;
    private String status;
    private List<PaymentProviderDTO> paymentProviders;
} 