package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "payment_sessions")
public class PaymentSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal amount;
    
    @Column(name = "currency_code")
    private String currencyCode;
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> data;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> context;
    
    private String status;
    
    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;
    
    @ManyToOne
    @JoinColumn(name = "payment_collection_id")
    private PaymentCollection paymentCollection;
    
    @OneToOne(mappedBy = "paymentSession", cascade = CascadeType.ALL)
    private Payment payment;
    
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