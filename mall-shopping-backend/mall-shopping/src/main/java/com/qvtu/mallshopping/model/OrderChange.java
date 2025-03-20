package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "order_changes")
public class OrderChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "version")
    private Integer version;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "return_id")
    private Long returnId;
    
    @Column(name = "exchange_id")
    private Long exchangeId;
    
    @Column(name = "claim_id")
    private Long claimId;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "requested_by")
    private String requestedBy;
    
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    
    @Column(name = "confirmed_by")
    private String confirmedBy;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "declined_by")
    private String declinedBy;
    
    @Column(name = "declined_reason")
    private String declinedReason;
    
    @Column(name = "declined_at")
    private LocalDateTime declinedAt;
    
    @Column(name = "canceled_by")
    private String canceledBy;
    
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 