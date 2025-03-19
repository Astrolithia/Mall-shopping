package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    Page<Campaign> findByDeletedAtIsNull(Pageable pageable);
    
    Page<Campaign> findByNameContainingAndDeletedAtIsNull(String name, Pageable pageable);
} 