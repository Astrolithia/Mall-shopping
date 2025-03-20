package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.OrderChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderChangeRepository extends JpaRepository<OrderChange, Long> {
    List<OrderChange> findByOrderIdOrderByCreatedAtDesc(Long orderId);
} 