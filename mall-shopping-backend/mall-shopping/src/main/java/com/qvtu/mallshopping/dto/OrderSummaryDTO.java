package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderSummaryDTO {
    private BigDecimal paidTotal;
    private BigDecimal refundedTotal;
    private BigDecimal pendingDifference;
    private BigDecimal currentOrderTotal;
    private BigDecimal originalOrderTotal;
    private BigDecimal transactionTotal;
    private BigDecimal accountingTotal;
} 