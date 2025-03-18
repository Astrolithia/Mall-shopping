package com.qvtu.mallshopping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qvtu.mallshopping.model.Promotion;
import com.qvtu.mallshopping.repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvtu.mallshopping.dto.PromotionCreateRequest;
import com.qvtu.mallshopping.dto.PromotionCreateRequest.ApplicationMethod;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    public Map<String, Object> listPromotions(int page, int size) {
        log.info("获取促销活动列表, 页码: {}, 每页数量: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionRepository.findByDeletedAtIsNull(pageable);
        
        List<Map<String, Object>> formattedPromotions = promotionPage.getContent().stream()
            .map(this::formatPromotionResponse)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("promotions", formattedPromotions);
        response.put("count", promotionPage.getTotalElements());
        response.put("offset", page * size);
        response.put("limit", size);

        return response;
    }
    
    private Map<String, Object> formatPromotionResponse(Promotion promotion) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", promotion.getId().toString());
        formatted.put("code", promotion.getCode());
        formatted.put("type", promotion.getType());
        formatted.put("is_automatic", promotion.isAutomatic());
        formatted.put("campaign_id", promotion.getCampaignId());
        formatted.put("status", promotion.getStatus());
        formatted.put("created_at", promotion.getCreatedAt());
        formatted.put("updated_at", promotion.getUpdatedAt());
        formatted.put("deleted_at", promotion.getDeletedAt());
        
        // 这里可以添加更多字段，如rules和application_method的解析
        
        return formatted;
    }

    public Map<String, Object> createPromotion(PromotionCreateRequest request) throws JsonProcessingException {
        log.info("开始创建促销活动, 请求数据: {}", request);
        
        try {
            // 检查促销码是否已存在
            if (promotionRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Promotion with this code already exists");
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 创建新促销活动
            Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .type(request.getType())
                .isAutomatic(request.isAutomatic())
                .campaignId(request.getCampaignId())
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .rules(request.getRules() != null ? objectMapper.writeValueAsString(request.getRules()) : null)
                .applicationMethod(request.getApplicationMethod() != null ? objectMapper.writeValueAsString(request.getApplicationMethod()) : null)
                .build();
            
            promotion = promotionRepository.save(promotion);
            log.info("促销活动创建成功, ID: {}", promotion.getId());
            
            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", formatPromotionResponse(promotion));
            
            return response;
        } catch (Exception e) {
            log.error("创建促销活动失败", e);
            throw e;
        }
    }

    public Map<String, Object> seedPromotions() throws JsonProcessingException {
        log.info("开始生成测试促销活动数据");
        
        try {
            List<Promotion> promotions = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 示例1: 固定金额折扣
            ApplicationMethod fixedDiscount = new ApplicationMethod();
            fixedDiscount.setDescription("满100元减10元");
            fixedDiscount.setValue(10.0);
            fixedDiscount.setCurrencyCode("CNY");
            fixedDiscount.setType("fixed");
            fixedDiscount.setTargetType("items");
            fixedDiscount.setAllocation("each");
            
            Promotion promotion1 = Promotion.builder()
                .code("FIXED10")
                .type("standard")
                .isAutomatic(false)
                .status("active")
                .applicationMethod(objectMapper.writeValueAsString(fixedDiscount))
                .build();
            
            promotions.add(promotionRepository.save(promotion1));
            
            // 示例2: 百分比折扣
            ApplicationMethod percentageDiscount = new ApplicationMethod();
            percentageDiscount.setDescription("全场8折");
            percentageDiscount.setValue(20.0); // 20% 折扣
            percentageDiscount.setType("percentage");
            percentageDiscount.setTargetType("items");
            percentageDiscount.setAllocation("each");
            
            Promotion promotion2 = Promotion.builder()
                .code("PERCENT20")
                .type("standard")
                .isAutomatic(true)
                .status("active")
                .applicationMethod(objectMapper.writeValueAsString(percentageDiscount))
                .build();
            
            promotions.add(promotionRepository.save(promotion2));
            
            // 示例3: 买一送一
            ApplicationMethod buyXGetY = new ApplicationMethod();
            buyXGetY.setDescription("买一送一");
            buyXGetY.setType("free_shipping");
            buyXGetY.setTargetType("items");
            buyXGetY.setAllocation("each");
            buyXGetY.setBuyRulesMinQuantity(1);
            buyXGetY.setApplyToQuantity(1);
            
            Promotion promotion3 = Promotion.builder()
                .code("BUY1GET1")
                .type("buy_x_get_y")
                .isAutomatic(false)
                .status("draft")
                .applicationMethod(objectMapper.writeValueAsString(buyXGetY))
                .build();
            
            promotions.add(promotionRepository.save(promotion3));
            
            // 格式化响应
            List<Map<String, Object>> formattedPromotions = promotions.stream()
                .map(this::formatPromotionResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotions", formattedPromotions);
            response.put("count", promotions.size());
            
            log.info("测试促销活动数据生成完成, 共 {} 条", promotions.size());
            return response;
        } catch (Exception e) {
            log.error("生成测试促销活动数据失败", e);
            throw e;
        }
    }
} 