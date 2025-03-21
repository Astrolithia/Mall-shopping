package com.qvtu.mallshopping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 商城前台 API 的 CORS 配置
        registry.addMapping("/store/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        // 后台管理 API 的 CORS 配置
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:9000", "http://localhost:8080", "http://localhost:8000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")         // 后台管理接口需要认证
                .excludePathPatterns("/api/auth/**") // 排除后台登录接口
                .excludePathPatterns("/store/**");   // 排除商城前台接口
    }
} 