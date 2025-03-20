package com.qvtu.mallshopping.config;

import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查测试用户是否已存在
        if (!customerRepository.findByEmail("test@example.com").isPresent()) {
            Customer testCustomer = Customer.builder()
                    .email("test@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Test")
                    .lastName("User")
                    .phone("1234567890")
                    .hasAccount(true)
                    .build();
            
            customerRepository.save(testCustomer);
        }
    }
} 