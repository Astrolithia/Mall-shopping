package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerCreateRequest;
import com.qvtu.mallshopping.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@Slf4j
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCustomers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取客户列表请求");
        Map<String, Object> response = customerService.listCustomers(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody CustomerCreateRequest request) {
        log.info("收到创建客户请求: {}", request);
        Map<String, Object> response = customerService.createCustomer(request);
        return ResponseEntity.ok(response);
    }
} 