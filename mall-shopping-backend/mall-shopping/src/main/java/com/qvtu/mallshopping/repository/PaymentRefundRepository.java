package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
} 