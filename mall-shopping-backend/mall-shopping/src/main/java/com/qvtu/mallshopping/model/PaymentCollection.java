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
@Table(name = "payment_collections")
public class PaymentCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "currency_code")
    private String currencyCode;
    
    private BigDecimal amount;
    
    @Column(name = "authorized_amount")
    private BigDecimal authorizedAmount;
    
    @Column(name = "captured_amount")
    private BigDecimal capturedAmount;
    
    @Column(name = "refunded_amount")
    private BigDecimal refundedAmount;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> metadata;
    
    private String status;
    
    @OneToMany(mappedBy = "paymentCollection", cascade = CascadeType.ALL)
    private List<PaymentSession> paymentSessions;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "paymentCollection", fetch = FetchType.LAZY)
    private List<PaymentProvider> paymentProviders;
    
    public List<PaymentProvider> getPaymentProviders() {
        return paymentProviders;
    }
    
    public void setPaymentProviders(List<PaymentProvider> paymentProviders) {
        this.paymentProviders = paymentProviders;
    }
} 