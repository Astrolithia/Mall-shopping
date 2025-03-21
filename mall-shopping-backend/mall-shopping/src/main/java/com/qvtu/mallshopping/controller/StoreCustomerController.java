package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerRegisterRequest;
import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/store")
@Slf4j
public class StoreCustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/customers")
    public ResponseEntity<Map<String, Object>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        log.info("收到商城客户注册请求: {}", request.getEmail());
        Customer customer = customerService.createCustomer(request);
        Map<String, Object> response = new HashMap<>();
        response.put("customer", formatCustomerResponse(customer));
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