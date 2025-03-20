package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_refunds")
public class PaymentRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal amount;
    
    @Column(name = "refund_reason_id")
    private String refundReasonId;
    
    private String note;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 