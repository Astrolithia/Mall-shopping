package com.qvtu.mallshopping.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.qvtu.mallshopping.dto.PromotionCreateRequest.ApplicationMethod;
import com.qvtu.mallshopping.model.Promotion;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class PromotionDataGenerator {
    private final Faker faker;
    private final Random random;
    private final ObjectMapper objectMapper;

    public PromotionDataGenerator() {
        this.faker = new Faker(new Locale("zh-CN"));
        this.random = new Random();
        this.objectMapper = new ObjectMapper();
    }

    public Promotion generatePromotion() {
        try {
            // 生成促销代码
            int minLength = 2;
            int maxLength = 6;
            int length = minLength + random.nextInt(maxLength - minLength + 1);
            String code = faker.commerce().promotionCode(length);
            
            // 随机选择促销类型
            String[] types = {"standard", "buy_x_get_y", "free_shipping"};
            String type = types[random.nextInt(types.length)];
            
            // 随机选择状态
            String[] statuses = {"draft", "active", "expired"};
            String status = statuses[random.nextInt(statuses.length)];
            
            // 创建应用方法
            ApplicationMethod applicationMethod = generateApplicationMethod(type);
            
            // 生成规则
            List<Map<String, Object>> rules = generateRules();
            
            // 创建促销活动
            return Promotion.builder()
                .code(code)
                .type(type)
                .isAutomatic(random.nextBoolean())
                .campaignId(faker.idNumber().valid())
                .status(status)
                .rules(objectMapper.writeValueAsString(rules))
                .applicationMethod(objectMapper.writeValueAsString(applicationMethod))
                .build();
        } catch (Exception e) {
            log.error("生成随机促销活动失败", e);
            throw new RuntimeException("生成随机促销活动失败", e);
        }
    }

    public List<Promotion> generatePromotions(int count) {
        List<Promotion> promotions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            promotions.add(generatePromotion());
        }
        return promotions;
    }

    private ApplicationMethod generateApplicationMethod(String type) {
        ApplicationMethod applicationMethod = new ApplicationMethod();
        applicationMethod.setDescription(faker.commerce().productName() + "促销");
        
        switch (type) {
            case "standard":
                applicationMethod.setType("fixed");
                applicationMethod.setValue(random.nextDouble() * 100);
                applicationMethod.setCurrencyCode("CNY");
                applicationMethod.setTargetType("items");
                applicationMethod.setAllocation("each");
                break;
                
            case "buy_x_get_y":
                applicationMethod.setType("percentage");
                applicationMethod.setValue(random.nextDouble() * 50);
                applicationMethod.setTargetType("items");
                applicationMethod.setAllocation("each");
                applicationMethod.setBuyRulesMinQuantity(random.nextInt(3) + 1);
                applicationMethod.setApplyToQuantity(random.nextInt(2) + 1);
                break;
                
            case "free_shipping":
                applicationMethod.setType("free_shipping");
                applicationMethod.setTargetType("shipping");
                applicationMethod.setAllocation("total");
                break;
        }
        
        return applicationMethod;
    }

    private List<Map<String, Object>> generateRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        int ruleCount = random.nextInt(3) + 1;
        
        for (int i = 0; i < ruleCount; i++) {
            Map<String, Object> rule = new HashMap<>();
            rule.put("id", "rule_" + (i + 1));
            rule.put("description", faker.commerce().department() + "相关规则");
            
            String[] attributes = {"order_total", "item_quantity", "customer_group"};
            String attribute = attributes[random.nextInt(attributes.length)];
            rule.put("attribute", attribute);
            
            switch (attribute) {
                case "order_total":
                    rule.put("operator", "gt");
                    rule.put("values", List.of(String.valueOf(random.nextInt(1000) + 100)));
                    break;
                    
                case "item_quantity":
                    rule.put("operator", "gte");
                    rule.put("values", List.of(String.valueOf(random.nextInt(5) + 1)));
                    break;
                    
                case "customer_group":
                    rule.put("operator", "in");
                    rule.put("values", List.of(String.valueOf(random.nextInt(2) + 1)));
                    break;
            }
            
            rules.add(rule);
        }
        
        return rules;
    }
} 