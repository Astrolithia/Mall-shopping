package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerAuthRequest;
import com.qvtu.mallshopping.service.CustomerAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/store/auth")
public class CustomerAuthController {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerAuthController.class);
    
    @Autowired
    private CustomerAuthService customerAuthService;
    
    @PostMapping("/customer/emailpass")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody CustomerAuthRequest request) {
        log.info("Received authentication request for email: {}", request.getEmail());
        try {
            String token = customerAuthService.authenticate(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }
} 