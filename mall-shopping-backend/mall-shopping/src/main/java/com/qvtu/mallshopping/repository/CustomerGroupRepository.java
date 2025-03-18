package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    boolean existsByName(String name);
} 