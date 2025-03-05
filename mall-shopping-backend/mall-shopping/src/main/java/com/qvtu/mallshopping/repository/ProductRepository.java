package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByHandle(String handle);
}