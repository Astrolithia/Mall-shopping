package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findAll(Pageable pageable);
    Optional<Customer> findByEmail(String email);
    
    // 添加这个方法来检查邮箱是否存在
    boolean existsByEmail(String email);

    Optional<Customer> findByResetPasswordTokenEquals(String resetPasswordToken);
} 