package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "collections")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true)
    private String handle;

    private String thumbnail;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    @JsonIgnore  // 防止循环引用
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    // 添加辅助方法来管理双向关系
    public void addProduct(Product product) {
        products.add(product);
        product.setCollection(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setCollection(null);
    }

    // 添加 getter 和 setter
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
