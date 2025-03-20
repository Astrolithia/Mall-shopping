package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.CustomerAuthRequest;
import com.qvtu.mallshopping.dto.CustomerAuthResponse;
import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.repository.CustomerRepository;
import com.qvtu.mallshopping.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerAuthService {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public String authenticate(CustomerAuthRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        return jwtUtil.generateToken(customer);
    }
} 