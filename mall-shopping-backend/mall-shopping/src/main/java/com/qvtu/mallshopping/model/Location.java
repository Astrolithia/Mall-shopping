package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "stock_locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String address;
    private String city;
    
    @Column(name = "country_code")
    private String countryCode;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    private String phone;
    private String province;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @OneToMany(mappedBy = "location")
    private List<Inventory> inventories = new ArrayList<>();
    
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