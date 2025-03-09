package com.qvtu.mallshopping.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qvtu.mallshopping.dto.SimpleProductDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

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

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    // 只在获取系列详情时使用
    @JsonProperty("products")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Map<String, Object>> getProductDTOs() {
        // 如果不包含产品数据或产品列表为空，返回空列表而不是 null
        if (!includeProducts || products == null) {
            return new ArrayList<>();
        }
        
        return products.stream()
            .map(product -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", product.getId());
                dto.put("title", product.getTitle());
                dto.put("subtitle", product.getSubtitle());
                dto.put("description", product.getDescription());
                dto.put("handle", product.getHandle());
                dto.put("thumbnail", product.getThumbnail());
                dto.put("status", product.getStatus());
                dto.put("weight", product.getWeight());
                dto.put("length", product.getLength());
                dto.put("height", product.getHeight());
                dto.put("width", product.getWidth());
                dto.put("created_at", product.getCreatedAt());
                dto.put("updated_at", product.getUpdatedAt());
                return dto;
            })
            .collect(Collectors.toList());
    }

    // 添加一个标志来控制是否包含产品数据
    @Transient
    private boolean includeProducts = false;

    public void setIncludeProducts(boolean includeProducts) {
        this.includeProducts = includeProducts;
    }

    // 修改添加产品的方法
    public void addProduct(Product product) {
        if (product != null) {
            if (products == null) {
                products = new ArrayList<>();
            }
            if (!products.contains(product)) {
                products.add(product);
                product.setCollection(this);
            }
        }
    }

    // 修改移除产品的方法
    public void removeProduct(Product product) {
        if (product != null && products != null) {
            products.remove(product);
            product.setCollection(null);
        }
    }

    // 添加 getter 和 setter
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // 修改 products_count 方法
    @JsonProperty("products_count")
    public int getProductsCount() {
        return products != null ? products.size() : 0;
    }

    // 修改 toString 方法避免循环引用
    @Override
    public String toString() {
        return "Collection{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", handle='" + handle + '\'' +
                ", productsCount=" + (products != null ? products.size() : 0) +
                '}';
    }

    // 修改 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collection)) return false;
        Collection that = (Collection) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
