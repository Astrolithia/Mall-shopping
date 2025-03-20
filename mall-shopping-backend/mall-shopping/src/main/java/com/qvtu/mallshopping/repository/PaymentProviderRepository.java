package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, Long> {
    List<PaymentProvider> findByIsEnabled(Boolean isEnabled);
} 