package com.qvtu.mallshopping.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 对于OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        // 暂时关闭token验证，允许所有请求通过
        return true;
        
        // TODO: 之后可以添加proper token验证
        /*
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 验证Medusa token
            return true;
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
        */
    }
} 