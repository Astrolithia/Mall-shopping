package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findByDeletedAtIsNull(Pageable pageable);
    boolean existsByCode(String code);
    Optional<Promotion> findByCode(String code);
    List<Promotion> findAllByCode(String code);
    Page<Promotion> findByCampaignId(String campaignId, Pageable pageable);
} 