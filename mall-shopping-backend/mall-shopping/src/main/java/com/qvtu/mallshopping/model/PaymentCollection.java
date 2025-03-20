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
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_collection_order"))
    private Order order;
    
    @Column(nullable = false)
    private String currencyCode;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(name = "authorized_amount")
    private BigDecimal authorizedAmount;
    
    @Column(name = "captured_amount")
    private BigDecimal capturedAmount;
    
    @Column(name = "refunded_amount")
    private BigDecimal refundedAmount;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(columnDefinition = "jsonb")
    private String metadata;
    
    @Column(nullable = false)
    private String status;
    
    @OneToMany(mappedBy = "paymentCollection", cascade = CascadeType.ALL)
    private List<PaymentSession> paymentSessions;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
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