package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.PaymentCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCollectionRepository extends JpaRepository<PaymentCollection, Long> {
} 