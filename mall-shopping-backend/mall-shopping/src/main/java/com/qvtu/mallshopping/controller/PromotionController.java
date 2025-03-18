package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.qvtu.mallshopping.dto.PromotionCreateRequest;

@RestController
@RequestMapping("/api/promotions")
@Slf4j
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPromotions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取促销活动列表请求, page: {}, size: {}", page, size);
        try {
            Map<String, Object> response = promotionService.listPromotions(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取促销活动列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPromotion(@RequestBody PromotionCreateRequest request) {
        log.info("收到创建促销活动请求");
        try {
            Map<String, Object> response = promotionService.createPromotion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建促销活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedPromotions() {
        log.info("收到生成测试促销活动数据请求");
        try {
            Map<String, Object> response = promotionService.seedPromotions();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成测试促销活动数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 