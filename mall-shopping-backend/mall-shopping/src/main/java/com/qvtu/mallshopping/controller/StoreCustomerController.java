package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerRegisterRequest;
import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.service.CustomerService;
import com.qvtu.mallshopping.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/store/customers")
@Slf4j
public class StoreCustomerController {
    
    private final CustomerService customerService;
    private final JwtTokenProvider jwtTokenProvider;

    public StoreCustomerController(CustomerService customerService, JwtTokenProvider jwtTokenProvider) {
        this.customerService = customerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        log.info("收到商城客户注册请求: {}", request.getEmail());
        Customer customer = customerService.registerCustomer(request);
        
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(customer.getId());
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);  // 只返回令牌
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> formatCustomerResponse(Customer customer) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", customer.getId().toString());
        formatted.put("email", customer.getEmail());
        formatted.put("first_name", customer.getFirstName());
        formatted.put("last_name", customer.getLastName());
        formatted.put("phone", customer.getPhone());
        formatted.put("has_account", customer.getHasAccount());
        formatted.put("created_at", customer.getCreatedAt());
        formatted.put("updated_at", customer.getUpdatedAt());
        return formatted;
    }
} 