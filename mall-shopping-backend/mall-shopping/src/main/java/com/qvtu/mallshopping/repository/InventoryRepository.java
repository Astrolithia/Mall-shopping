package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySku(String sku);
    Page<Inventory> findAll(Pageable pageable);
} 