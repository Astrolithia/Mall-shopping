package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import java.util.Map;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String handle;

    private Boolean isGiftcard = false;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private Boolean discountable = true;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnail;

    private Double weight;
    private Double length;
    private Double height;
    private Double width;

    private String originCountry;
    private String hsCode;
    private String midCode;
    private String material;

    private String externalId;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
