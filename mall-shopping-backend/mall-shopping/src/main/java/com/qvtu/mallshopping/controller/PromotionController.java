package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Promotion;
import com.qvtu.mallshopping.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import com.qvtu.mallshopping.dto.PromotionCreateRequest;
import com.qvtu.mallshopping.dto.RuleAttributeDTO;
import com.qvtu.mallshopping.dto.RuleValueDTO;
import com.qvtu.mallshopping.dto.RuleDTO;
import com.qvtu.mallshopping.dto.RuleBatchRequestDTO;
import com.qvtu.mallshopping.dto.PromotionUpdateRequest;

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

    @GetMapping("/rule-attributes")
    public ResponseEntity<Map<String, Object>> listRuleAttributes(
        @RequestParam(required = false) String rule_type
    ) {
        log.info("收到获取规则属性请求, 规则类型: {}", rule_type);
        try {
            List<RuleAttributeDTO> attributes = promotionService.listPotentialRuleAttributes(rule_type);
            Map<String, Object> response = new HashMap<>();
            response.put("rule_attributes", attributes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取规则属性失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rule-values")
    public ResponseEntity<Map<String, Object>> listRuleValues(
        @RequestParam(required = false) String rule_type,
        @RequestParam String rule_attribute_id
    ) {
        log.info("收到获取规则值请求, 规则类型: {}, 属性ID: {}", rule_type, rule_attribute_id);
        try {
            List<RuleValueDTO> values = promotionService.listRuleValueOptions(rule_type, rule_attribute_id);
            Map<String, Object> response = new HashMap<>();
            response.put("rule_values", values);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取规则值失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/rules")
    public ResponseEntity<Map<String, Object>> getPromotionRules(
        @PathVariable Long id,
        @RequestParam(required = false) String rule_type
    ) {
        log.info("收到获取促销活动规则请求, 促销ID: {}, 规则类型: {}", id, rule_type);
        try {
            List<RuleDTO> rules = promotionService.getPromotionRules(id, rule_type);
            Map<String, Object> response = new HashMap<>();
            response.put("rules", rules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取促销活动规则失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/rules/batch")
    public ResponseEntity<Map<String, Object>> updatePromotionRules(
        @PathVariable Long id,
        @RequestBody RuleBatchRequestDTO request
    ) {
        log.info("收到更新促销活动规则请求, 促销ID: {}", id);
        try {
            Promotion promotion = promotionService.updatePromotionRules(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotionService.formatPromotionResponse(promotion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新促销活动规则失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/buy-rules/batch")
    public ResponseEntity<Map<String, Object>> updatePromotionBuyRules(
        @PathVariable Long id,
        @RequestBody RuleBatchRequestDTO request
    ) {
        log.info("收到更新促销活动购买规则请求, 促销ID: {}", id);
        try {
            Promotion promotion = promotionService.updatePromotionBuyRules(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotionService.formatPromotionResponse(promotion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新促销活动购买规则失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/target-rules/batch")
    public ResponseEntity<Map<String, Object>> updatePromotionTargetRules(
        @PathVariable Long id,
        @RequestBody RuleBatchRequestDTO request
    ) {
        log.info("收到更新促销活动目标规则请求, 促销ID: {}", id);
        try {
            Promotion promotion = promotionService.updatePromotionTargetRules(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotionService.formatPromotionResponse(promotion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新促销活动目标规则失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/seed-rules")
    public ResponseEntity<Map<String, Object>> seedPromotionRules() {
        log.info("收到生成促销活动规则测试数据请求");
        try {
            Map<String, Object> response = promotionService.seedPromotionRules();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成促销活动规则测试数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> clearAllPromotions() {
        log.info("收到清理所有促销活动数据请求");
        try {
            promotionService.clearAllPromotions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("清理促销活动数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPromotion(@PathVariable Long id) {
        log.info("收到获取促销活动请求, ID: {}", id);
        try {
            Map<String, Object> response = promotionService.getPromotion(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取促销活动失败", e);
            if (e.getMessage().equals("Promotion not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePromotion(
        @PathVariable Long id,
        @RequestBody PromotionUpdateRequest request
    ) {
        log.info("收到更新促销活动请求, ID: {}", id);
        try {
            Map<String, Object> response = promotionService.updatePromotion(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新促销活动失败", e);
            if (e.getMessage().equals("Promotion not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().equals("Promotion with this code already exists")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "促销代码已存在"
                ));
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/random")
    public ResponseEntity<Map<String, Object>> generateRandomPromotion() {
        log.info("收到生成随机促销活动请求");
        try {
            Map<String, Object> response = promotionService.generateRandomPromotion();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成随机促销活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/random/batch")
    public ResponseEntity<Map<String, Object>> generateRandomPromotions(
        @RequestParam(defaultValue = "5") int count
    ) {
        log.info("收到批量生成随机促销活动请求, 数量: {}", count);
        try {
            Map<String, Object> response = promotionService.generateRandomPromotions(count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成随机促销活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePromotion(@PathVariable Long id) {
        log.info("收到删除促销活动请求, ID: {}", id);
        try {
            Map<String, Object> response = promotionService.deletePromotion(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除促销活动失败", e);
            if (e.getMessage().equals("Promotion not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }
} 