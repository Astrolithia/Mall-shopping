package com.qvtu.mallshopping.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Embeddable
public class OrderSummary {
    @Column(name = "paid_total")
    private BigDecimal paidTotal;
    
    @Column(name = "refunded_total")
    private BigDecimal refundedTotal;
    
    @Column(name = "pending_difference")
    private BigDecimal pendingDifference;
    
    @Column(name = "current_order_total")
    private BigDecimal currentOrderTotal;
    
    @Column(name = "original_order_total")
    private BigDecimal originalOrderTotal;
    
    @Column(name = "transaction_total")
    private BigDecimal transactionTotal;
    
    @Column(name = "accounting_total")
    private BigDecimal accountingTotal;
} 