package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_providers")
public class PaymentProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_id", unique = true)
    private String providerId;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_collection_id")
    private PaymentCollection paymentCollection;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public PaymentCollection getPaymentCollection() {
        return paymentCollection;
    }

    public void setPaymentCollection(PaymentCollection paymentCollection) {
        this.paymentCollection = paymentCollection;
    }
} 