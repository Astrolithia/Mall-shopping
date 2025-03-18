package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerCreateRequest;
import com.qvtu.mallshopping.dto.CustomerUpdateRequest;
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

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable Long id) {
        log.info("收到获取客户详情请求, ID: {}", id);
        Map<String, Object> response = customerService.getCustomer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
        @PathVariable Long id,
        @RequestBody CustomerUpdateRequest request
    ) {
        log.info("收到更新客户请求, ID: {}, 请求数据: {}", id, request);
        Map<String, Object> response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        log.info("收到删除客户请求, ID: {}", id);
        Map<String, Object> response = customerService.deleteCustomer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedCustomers() {
        log.info("收到生成测试客户数据请求");
        Map<String, Object> response = customerService.seedCustomers();
        return ResponseEntity.ok(response);
    }
} 