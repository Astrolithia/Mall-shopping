package com.qvtu.mallshopping.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String handle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "rank")
    private Integer rank = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_category_id")
    @ToString.Exclude
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Category> children = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "category_metadata",
            joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> metadata = new HashMap<>();

    public void setParentCategory(Category parent) {
        this.parentCategory = parent;
        if (parent != null && !parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }

    public void addChild(Category child) {
        children.add(child);
        if (child.getParentCategory() != this) {
            child.setParentCategory(this);
        }
    }

    public Long getParentCategoryId() {
        return parentCategory != null ? parentCategory.getId() : null;
    }

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
