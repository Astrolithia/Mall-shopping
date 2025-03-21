package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Page<Location> findByDeletedAtIsNull(Pageable pageable);
} 