package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        // 这里简单实现,实际应该查询数据库验证用户
        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer test-token"); // 实际应该生成JWT
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(null);
    }
} 