package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    private BigDecimal amount;
    
    @Column(name = "currency_code")
    private String currencyCode;
    
    // 交易引用类型，如：capture, refund, authorization 等
    private String reference;
    
    // 外部支付系统的交易ID
    @Column(name = "reference_id")
    private String referenceId;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> metadata;
    
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