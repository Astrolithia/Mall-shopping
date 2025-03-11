package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySku(String sku);
    Page<Inventory> findAll(Pageable pageable);
    Page<Inventory> findByDeletedAtIsNull(Pageable pageable);
    Optional<Inventory> findByIdAndDeletedAtIsNull(Long id);
} 