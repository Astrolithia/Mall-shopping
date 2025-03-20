package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
} 