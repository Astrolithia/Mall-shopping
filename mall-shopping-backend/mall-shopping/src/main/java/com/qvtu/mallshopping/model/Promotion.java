package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "promotions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    
    private String type; // standard, buy_x_get_y, etc.
    
    private boolean isAutomatic;
    
    private String campaignId;
    
    private String status; // draft, active, expired, etc.
    
    @Column(columnDefinition = "TEXT")
    private String rules; // 存储为JSON字符串
    
    @Column(columnDefinition = "TEXT")
    private String applicationMethod; // 存储为JSON字符串
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
} 