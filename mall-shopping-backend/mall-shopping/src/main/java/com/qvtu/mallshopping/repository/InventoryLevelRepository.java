package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.InventoryLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Long> {
    Page<InventoryLevel> findByInventoryIdAndDeletedAtIsNull(Long inventoryId, Pageable pageable);
} 