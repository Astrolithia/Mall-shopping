package com.qvtu.mallshopping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.model.Promotion;
import com.qvtu.mallshopping.repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvtu.mallshopping.dto.PromotionCreateRequest.ApplicationMethod;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.javafaker.Faker;
import java.util.Locale;
import java.util.Random;
import com.qvtu.mallshopping.generator.PromotionDataGenerator;

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
    
    public Map<String, Object> formatPromotionResponse(Promotion promotion) {
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
        
        // 解析规则和应用方法
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            
            if (promotion.getRules() != null && !promotion.getRules().isEmpty()) {
                formatted.put("rules", objectMapper.readValue(promotion.getRules(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)));
            }
            
            if (promotion.getApplicationMethod() != null && !promotion.getApplicationMethod().isEmpty()) {
                formatted.put("application_method", objectMapper.readValue(promotion.getApplicationMethod(), Map.class));
            }
        } catch (Exception e) {
            log.error("解析促销活动规则或应用方法失败", e);
        }
        
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

    /**
     * 列出潜在规则属性
     */
    public List<RuleAttributeDTO> listPotentialRuleAttributes(String ruleType) {
        log.info("获取潜在规则属性, 规则类型: {}", ruleType);
        
        // 这里根据规则类型返回不同的属性列表
        List<RuleAttributeDTO> attributes = new ArrayList<>();
        
        if ("standard".equals(ruleType) || ruleType == null) {
            // 标准规则的属性
            attributes.add(RuleAttributeDTO.builder()
                .id("order_total")
                .name("订单总金额")
                .description("订单的总金额")
                .type("number")
                .build());
            
            attributes.add(RuleAttributeDTO.builder()
                .id("item_quantity")
                .name("商品数量")
                .description("购物车中的商品数量")
                .type("number")
                .build());
            
            attributes.add(RuleAttributeDTO.builder()
                .id("customer_group")
                .name("客户群组")
                .description("客户所属的群组")
                .type("string")
                .build());
        } else if ("buy_x_get_y".equals(ruleType)) {
            // 买X送Y规则的属性
            attributes.add(RuleAttributeDTO.builder()
                .id("product_id")
                .name("商品ID")
                .description("特定商品的ID")
                .type("string")
                .build());
            
            attributes.add(RuleAttributeDTO.builder()
                .id("product_category")
                .name("商品类别")
                .description("商品所属的类别")
                .type("string")
                .build());
        }
        
        return attributes;
    }

    /**
     * 列出规则值选项
     */
    public List<RuleValueDTO> listRuleValueOptions(String ruleType, String attributeId) {
        log.info("获取规则值选项, 规则类型: {}, 属性ID: {}", ruleType, attributeId);
        
        List<RuleValueDTO> values = new ArrayList<>();
        
        if ("customer_group".equals(attributeId)) {
            // 如果是客户群组属性，返回所有客户群组
            values.add(RuleValueDTO.builder()
                .value("1")
                .label("VIP客户")
                .build());
            
            values.add(RuleValueDTO.builder()
                .value("2")
                .label("普通客户")
                .build());
        } else if ("product_category".equals(attributeId)) {
            // 如果是商品类别属性，返回所有商品类别
            values.add(RuleValueDTO.builder()
                .value("1")
                .label("电子产品")
                .build());
            
            values.add(RuleValueDTO.builder()
                .value("2")
                .label("服装")
                .build());
        }
        
        return values;
    }

    /**
     * 获取促销活动的规则
     */
    public List<RuleDTO> getPromotionRules(Long promotionId, String ruleType) throws Exception {
        log.info("获取促销活动规则, 促销ID: {}, 规则类型: {}", promotionId, ruleType);
        
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new Exception("Promotion not found"));
        
        if (promotion.getRules() == null) {
            return new ArrayList<>();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rulesMap = objectMapper.readValue(promotion.getRules(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        
        List<RuleDTO> rules = new ArrayList<>();
        for (Map<String, Object> ruleMap : rulesMap) {
            RuleDTO rule = RuleDTO.builder()
                .id(ruleMap.get("id").toString())
                .description(ruleMap.get("description") != null ? ruleMap.get("description").toString() : null)
                .type(ruleMap.get("type") != null ? ruleMap.get("type").toString() : null)
                .attribute(ruleMap.get("attribute") != null ? ruleMap.get("attribute").toString() : null)
                .operator(ruleMap.get("operator") != null ? ruleMap.get("operator").toString() : null)
                .build();
            
            if (ruleMap.get("values") != null) {
                rule.setValues((List<String>) ruleMap.get("values"));
            }
            
            rules.add(rule);
        }
        
        return rules;
    }

    /**
     * 更新促销活动的规则
     */
    public Promotion updatePromotionRules(Long promotionId, RuleBatchRequestDTO request) throws Exception {
        log.info("更新促销活动规则, 促销ID: {}, 规则数量: {}", promotionId, request.getRules().size());
        
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new Exception("Promotion not found"));
        
        ObjectMapper objectMapper = new ObjectMapper();
        String rulesJson = objectMapper.writeValueAsString(request.getRules());
        promotion.setRules(rulesJson);
        
        return promotionRepository.save(promotion);
    }

    /**
     * 更新促销活动的购买规则
     */
    public Promotion updatePromotionBuyRules(Long promotionId, RuleBatchRequestDTO request) throws Exception {
        log.info("更新促销活动购买规则, 促销ID: {}, 规则数量: {}", promotionId, request.getRules().size());
        
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new Exception("Promotion not found"));
        
        if (promotion.getApplicationMethod() == null) {
            throw new Exception("Promotion does not have application method");
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> applicationMethod = objectMapper.readValue(promotion.getApplicationMethod(), Map.class);
        applicationMethod.put("buy_rules", request.getRules());
        
        promotion.setApplicationMethod(objectMapper.writeValueAsString(applicationMethod));
        return promotionRepository.save(promotion);
    }

    /**
     * 更新促销活动的目标规则
     */
    public Promotion updatePromotionTargetRules(Long promotionId, RuleBatchRequestDTO request) throws Exception {
        log.info("更新促销活动目标规则, 促销ID: {}, 规则数量: {}", promotionId, request.getRules().size());
        
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new Exception("Promotion not found"));
        
        if (promotion.getApplicationMethod() == null) {
            throw new Exception("Promotion does not have application method");
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> applicationMethod = objectMapper.readValue(promotion.getApplicationMethod(), Map.class);
        applicationMethod.put("target_rules", request.getRules());
        
        promotion.setApplicationMethod(objectMapper.writeValueAsString(applicationMethod));
        return promotionRepository.save(promotion);
    }

    /**
     * 生成促销活动规则测试数据
     */
    public Map<String, Object> seedPromotionRules() throws JsonProcessingException {
        log.info("开始生成促销活动规则测试数据");
        
        try {
            // 首先清理可能存在的重复数据
            cleanupDuplicatePromotions();
            
            ObjectMapper objectMapper = new ObjectMapper();
            List<Promotion> updatedPromotions = new ArrayList<>();
            
            // 1. 为第一个促销活动添加标准规则
            Promotion promotion1 = promotionRepository.findByCode("FIXED10")
                .orElseThrow(() -> new RuntimeException("Promotion FIXED10 not found"));
            
            List<RuleDTO> standardRules = new ArrayList<>();
            standardRules.add(RuleDTO.builder()
                .id("rule_1")
                .description("订单金额大于100元")
                .attribute("order_total")
                .operator("gt")
                .values(List.of("100"))
                .build());
            
            standardRules.add(RuleDTO.builder()
                .id("rule_2")
                .description("客户属于VIP群组")
                .attribute("customer_group")
                .operator("in")
                .values(List.of("1"))
                .build());
            
            promotion1.setRules(objectMapper.writeValueAsString(standardRules));
            updatedPromotions.add(promotionRepository.save(promotion1));
            
            // 2. 为第二个促销活动添加购买规则和目标规则
            Promotion promotion2 = promotionRepository.findByCode("PERCENT20")
                .orElseThrow(() -> new RuntimeException("Promotion PERCENT20 not found"));
            
            // 解析现有的应用方法
            Map<String, Object> applicationMethod = objectMapper.readValue(
                promotion2.getApplicationMethod(), Map.class);
            
            // 添加购买规则
            List<RuleDTO> buyRules = new ArrayList<>();
            buyRules.add(RuleDTO.builder()
                .id("buy_rule_1")
                .description("购买电子产品")
                .attribute("product_category")
                .operator("in")
                .values(List.of("1"))
                .build());
            
            applicationMethod.put("buy_rules", buyRules);
            
            // 添加目标规则
            List<RuleDTO> targetRules = new ArrayList<>();
            targetRules.add(RuleDTO.builder()
                .id("target_rule_1")
                .description("应用于服装类商品")
                .attribute("product_category")
                .operator("in")
                .values(List.of("2"))
                .build());
            
            applicationMethod.put("target_rules", targetRules);
            
            // 更新应用方法
            promotion2.setApplicationMethod(objectMapper.writeValueAsString(applicationMethod));
            updatedPromotions.add(promotionRepository.save(promotion2));
            
            // 3. 为第三个促销活动添加复杂规则组合
            Promotion promotion3 = promotionRepository.findByCode("BUY1GET1")
                .orElseThrow(() -> new RuntimeException("Promotion BUY1GET1 not found"));
            
            // 标准规则
            List<RuleDTO> complexRules = new ArrayList<>();
            complexRules.add(RuleDTO.builder()
                .id("complex_rule_1")
                .description("商品数量大于等于2")
                .attribute("item_quantity")
                .operator("gte")
                .values(List.of("2"))
                .build());
            
            promotion3.setRules(objectMapper.writeValueAsString(complexRules));
            
            // 解析现有的应用方法
            Map<String, Object> complexApplicationMethod = objectMapper.readValue(
                promotion3.getApplicationMethod(), Map.class);
            
            // 添加购买规则
            List<RuleDTO> complexBuyRules = new ArrayList<>();
            complexBuyRules.add(RuleDTO.builder()
                .id("complex_buy_rule_1")
                .description("购买任意商品")
                .attribute("product_id")
                .operator("exists")
                .values(List.of())
                .build());
            
            complexApplicationMethod.put("buy_rules", complexBuyRules);
            
            // 添加目标规则
            List<RuleDTO> complexTargetRules = new ArrayList<>();
            complexTargetRules.add(RuleDTO.builder()
                .id("complex_target_rule_1")
                .description("应用于同类商品")
                .attribute("product_category")
                .operator("eq")
                .values(List.of("same_category"))
                .build());
            
            complexApplicationMethod.put("target_rules", complexTargetRules);
            
            // 更新应用方法
            promotion3.setApplicationMethod(objectMapper.writeValueAsString(complexApplicationMethod));
            updatedPromotions.add(promotionRepository.save(promotion3));
            
            // 格式化响应
            List<Map<String, Object>> formattedPromotions = updatedPromotions.stream()
                .map(this::formatPromotionResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotions", formattedPromotions);
            response.put("count", updatedPromotions.size());
            
            log.info("促销活动规则测试数据生成完成, 共更新 {} 条促销活动", updatedPromotions.size());
            return response;
        } catch (Exception e) {
            log.error("生成促销活动规则测试数据失败", e);
            throw e;
        }
    }

    /**
     * 清理重复的促销活动数据
     */
    private void cleanupDuplicatePromotions() {
        log.info("清理重复的促销活动数据");
        
        List<String> codesToCheck = Arrays.asList("FIXED10", "PERCENT20", "BUY1GET1");
        
        for (String code : codesToCheck) {
            try {
                List<Promotion> duplicates = promotionRepository.findAllByCode(code);
                if (duplicates.size() > 1) {
                    log.warn("发现重复的促销代码: {}, 数量: {}", code, duplicates.size());
                    // 保留最新的一条记录，删除其他记录
                    duplicates.stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .skip(1)
                        .forEach(p -> {
                            log.info("删除重复的促销活动, ID: {}", p.getId());
                            promotionRepository.delete(p);
                        });
                }
            } catch (Exception e) {
                log.error("清理重复促销活动时出错, 代码: {}", code, e);
            }
        }
        
        promotionRepository.flush();
    }

    public void clearAllPromotions() {
        log.info("清理所有促销活动数据");
        promotionRepository.deleteAll();
        promotionRepository.flush();
    }

    /**
     * 获取单个促销活动
     */
    public Map<String, Object> getPromotion(Long id) throws Exception {
        log.info("获取促销活动, ID: {}", id);
        
        try {
            Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promotion not found"));
                
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", formatPromotionResponse(promotion));
            
            return response;
        } catch (Exception e) {
            log.error("获取促销活动失败, ID: {}", id, e);
            throw e;
        }
    }

    /**
     * 更新促销活动
     */
    public Map<String, Object> updatePromotion(Long id, PromotionUpdateRequest request) throws Exception {
        log.info("更新促销活动, ID: {}, 请求数据: {}", id, request);
        
        try {
            Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promotion not found"));
            
            // 如果要更新code，需要检查新的code是否已存在
            if (request.getCode() != null && !request.getCode().equals(promotion.getCode())) {
                if (promotionRepository.existsByCode(request.getCode())) {
                    throw new Exception("Promotion with this code already exists");
                }
                promotion.setCode(request.getCode());
            }
            
            // 更新其他字段
            if (request.getType() != null) {
                promotion.setType(request.getType());
            }
            if (request.getIsAutomatic() != null) {
                promotion.setAutomatic(request.getIsAutomatic());
            }
            if (request.getCampaignId() != null) {
                promotion.setCampaignId(request.getCampaignId());
            }
            if (request.getStatus() != null) {
                promotion.setStatus(request.getStatus());
            }
            
            // 更新规则和应用方法
            ObjectMapper objectMapper = new ObjectMapper();
            if (request.getRules() != null) {
                promotion.setRules(objectMapper.writeValueAsString(request.getRules()));
            }
            if (request.getApplicationMethod() != null) {
                promotion.setApplicationMethod(objectMapper.writeValueAsString(request.getApplicationMethod()));
            }
            
            promotion = promotionRepository.save(promotion);
            log.info("促销活动更新成功, ID: {}", promotion.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", formatPromotionResponse(promotion));
            
            return response;
        } catch (Exception e) {
            log.error("更新促销活动失败, ID: {}", id, e);
            throw e;
        }
    }

    /**
     * 生成随机促销活动
     */
    public Map<String, Object> generateRandomPromotion() {
        log.info("开始生成随机促销活动");
        
        try {
            PromotionDataGenerator generator = new PromotionDataGenerator();
            Promotion promotion = generator.generatePromotion();
            promotion = promotionRepository.save(promotion);
            
            log.info("随机促销活动生成成功, ID: {}", promotion.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", formatPromotionResponse(promotion));
            
            return response;
        } catch (Exception e) {
            log.error("生成随机促销活动失败", e);
            throw e;
        }
    }

    /**
     * 生成多个随机促销活动
     */
    public Map<String, Object> generateRandomPromotions(int count) {
        log.info("开始生成随机促销活动, 数量: {}", count);
        
        try {
            PromotionDataGenerator generator = new PromotionDataGenerator();
            List<Promotion> promotions = generator.generatePromotions(count);
            promotions = promotionRepository.saveAll(promotions);
            
            List<Map<String, Object>> formattedPromotions = promotions.stream()
                .map(this::formatPromotionResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotions", formattedPromotions);
            response.put("count", formattedPromotions.size());
            
            return response;
        } catch (Exception e) {
            log.error("生成随机促销活动失败", e);
            throw e;
        }
    }

    /**
     * 删除促销活动
     */
    public Map<String, Object> deletePromotion(Long id) throws Exception {
        log.info("删除促销活动, ID: {}", id);
        
        try {
            Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promotion not found"));
                
            // 软删除：设置删除时间而不是真正删除记录
            promotion.setDeletedAt(LocalDateTime.now());
            promotionRepository.save(promotion);
            
            log.info("促销活动删除成功, ID: {}", id);
            
            // 返回符合 Medusa Admin API 格式的响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", id.toString());
            response.put("object", "promotion");
            response.put("deleted", true);
            
            return response;
        } catch (Exception e) {
            log.error("删除促销活动失败, ID: {}", id, e);
            throw e;
        }
    }
} 