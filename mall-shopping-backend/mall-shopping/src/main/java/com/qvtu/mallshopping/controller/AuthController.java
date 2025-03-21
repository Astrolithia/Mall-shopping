package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.model.Address;
import com.qvtu.mallshopping.service.CustomerService;
import com.qvtu.mallshopping.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final CustomerService customerService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthController(CustomerService customerService, JwtTokenProvider jwtTokenProvider) {
        this.customerService = customerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @PostMapping("/customer/emailpass")
    public ResponseEntity<Map<String, Object>> loginCustomer(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        // 验证用户凭据并获取用户
        Customer customer = customerService.authenticateCustomer(email, password);
        
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(customer.getId());
        
        // 返回令牌
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/customer/emailpass/register")
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        // 创建基本客户
        Customer customer = customerService.createBasicCustomer(email, password);
        
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(customer.getId());
        
        // 返回令牌
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@RequestHeader("Authorization") String bearerToken) {
        try {
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 从令牌中获取用户ID
            Long userId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 获取用户信息
            Customer customer = customerService.getCustomerById(userId);
            
            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("user", formatUserResponse(customer));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    private Map<String, Object> formatUserResponse(Customer customer) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", customer.getId().toString());
        formatted.put("email", customer.getEmail());
        formatted.put("first_name", customer.getFirstName());
        formatted.put("last_name", customer.getLastName());
        
        if (customer.getDefaultBillingAddress() != null) {
            formatted.put("default_billing_address_id", customer.getDefaultBillingAddress().getId().toString());
        }
        
        if (customer.getDefaultShippingAddress() != null) {
            formatted.put("default_shipping_address_id", customer.getDefaultShippingAddress().getId().toString());
        }
        
        // 格式化地址
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            formatted.put("addresses", customer.getAddresses().stream()
                    .map(this::formatAddressResponse)
                    .collect(Collectors.toList()));
        }
        
        return formatted;
    }
    
    private Map<String, Object> formatAddressResponse(Address address) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", address.getId().toString());
        formatted.put("customer_id", address.getCustomer().getId().toString());
        formatted.put("company", address.getCompany());
        formatted.put("first_name", address.getFirstName());
        formatted.put("last_name", address.getLastName());
        formatted.put("address_1", address.getAddress1());
        formatted.put("address_2", address.getAddress2());
        formatted.put("city", address.getCity());
        formatted.put("country_code", address.getCountryCode());
        formatted.put("province", address.getProvince());
        formatted.put("postal_code", address.getPostalCode());
        formatted.put("phone", address.getPhone());
        formatted.put("is_default_shipping", address.isDefaultShipping());
        formatted.put("is_default_billing", address.isDefaultBilling());
        formatted.put("created_at", address.getCreatedAt());
        formatted.put("updated_at", address.getUpdatedAt());
        
        return formatted;
    }
} 