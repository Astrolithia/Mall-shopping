package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.PaymentCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCaptureRepository extends JpaRepository<PaymentCapture, Long> {
} 