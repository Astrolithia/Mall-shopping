package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal amount;
    
    @Column(name = "authorized_amount")
    private BigDecimal authorizedAmount;
    
    @Column(name = "currency_code")
    private String currencyCode;
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> data;
    
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
    
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    @Column(name = "captured_amount")
    private BigDecimal capturedAmount;
    
    @Column(name = "refunded_amount")
    private BigDecimal refundedAmount;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentCapture> captures;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentRefund> refunds;
    
    @OneToOne
    @JoinColumn(name = "payment_session_id")
    private PaymentSession paymentSession;
    
    @ManyToOne
    @JoinColumn(name = "payment_collection_id")
    private PaymentCollection paymentCollection;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 