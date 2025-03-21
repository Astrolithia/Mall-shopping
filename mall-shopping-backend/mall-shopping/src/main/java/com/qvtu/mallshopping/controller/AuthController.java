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
    
    @DeleteMapping("/session")
    public ResponseEntity<Map<String, Object>> deleteSession(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        // 可以在这里添加令牌黑名单或其他会话清理逻辑
        // 对于JWT，服务器端通常不需要特殊处理，客户端只需删除令牌
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String bearerToken) {
        try {
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long userId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 生成新的JWT令牌
            String newToken = jwtTokenProvider.generateToken(userId);
            
            // 返回新令牌
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * 重置用户密码
     * 使用重置密码令牌来重置密码
     */
    @PostMapping("/customer/emailpass/reset-password/token")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody Map<String, String> request) {
        
        try {
            // 从Authorization头中提取重置令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String resetToken = bearerToken.substring(7);
            
            // 获取新密码
            String password = request.get("password");
            
            // 使用重置令牌重置密码
            Customer customer = customerService.resetPassword(resetToken, password);
            
            // 生成新的JWT令牌
            String jwtToken = jwtTokenProvider.generateToken(customer.getId());
            
            // 返回令牌
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 请求密码重置令牌
     */
    @PostMapping("/customer/emailpass/reset-password")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("identifier");
        
        // 调用服务生成重置令牌并发送邮件
        customerService.requestPasswordReset(email);
        
        // 返回空响应
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新用户密码
     * 允许已登录用户更新自己的密码
     */
    @PostMapping("/customer/emailpass/update")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody Map<String, String> request) {
        
        try {
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long userId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 获取请求中的电子邮件和新密码
            String email = request.get("email");
            String newPassword = request.get("password");
            
            // 更新密码
            customerService.updatePassword(userId, email, newPassword);
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
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