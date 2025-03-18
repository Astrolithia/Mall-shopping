package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findByDeletedAtIsNull(Pageable pageable);
    boolean existsByCode(String code);
} 