package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByHandle(String handle);

    List<Product> findByTitleContainingAndStatus(String title, ProductStatus status);

    List<Product> findByTitleContainingAndStatus(String title, ProductStatus status, Pageable pageable);
    
    List<Product> findByTitleContaining(String title, Pageable pageable);

    List<Product> findByStatus(ProductStatus status, Pageable pageable);

    long countByTitleContainingAndStatus(String title, ProductStatus status);
    long countByTitleContaining(String title);
    long countByStatus(ProductStatus status);
}