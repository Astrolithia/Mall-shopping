package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    boolean existsByHandle(String handle);
    List<Collection> findByTitleContaining(String title, Pageable pageable);
    long countByTitleContaining(String title);
}
