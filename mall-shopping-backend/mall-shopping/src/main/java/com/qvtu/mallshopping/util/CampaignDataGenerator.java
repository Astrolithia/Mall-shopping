package com.qvtu.mallshopping.util;

import com.github.javafaker.Faker;
import com.qvtu.mallshopping.dto.Budget;
import com.qvtu.mallshopping.model.Campaign;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CampaignDataGenerator {
    private final Faker faker = new Faker(new Locale("zh_CN"));

    public List<Campaign> generateCampaigns(int count) {
        List<Campaign> campaigns = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            campaigns.add(generateCampaign());
        }
        return campaigns;
    }

    private Campaign generateCampaign() {
        // 生成开始时间：未来30天内
        LocalDateTime startDate = LocalDateTime.ofInstant(
            faker.date().future(30, TimeUnit.DAYS).toInstant(), 
            ZoneId.systemDefault()
        );
        
        // 生成结束时间：从开始时间算起的60天内
        LocalDateTime endDate = startDate.plusDays(
            faker.number().numberBetween(1, 60)
        );

        String[] campaignTypes = {"节日特惠", "限时促销", "新品上市", "会员专享", "清仓特卖"};
        String campaignType = campaignTypes[faker.number().numberBetween(0, campaignTypes.length)];

        return Campaign.builder()
            .name(campaignType + "-" + faker.commerce().promotionCode())
            .description(generateDescription())
            .campaignIdentifier("CAM-" + faker.number().digits(8))
            .currency("CNY")
            .startsAt(startDate)
            .endsAt(endDate)
            .budget(generateBudgetJson())
            .build();
    }

    private String generateDescription() {
        String[] descriptions = {
            faker.commerce().productName() + "专场优惠",
            "限时" + faker.number().numberBetween(1, 7) + "天",
            "全场低至" + faker.number().numberBetween(1, 9) + "折",
            "满" + faker.number().numberBetween(100, 1000) + "减" + faker.number().numberBetween(10, 100),
            "新人专享特惠"
        };
        return descriptions[faker.number().numberBetween(0, descriptions.length)];
    }

    private String generateBudgetJson() {
        Budget budget = new Budget();
        budget.setType("spend");
        budget.setCurrencyCode("CNY");
        budget.setLimit(faker.number().randomDouble(2, 1000, 100000));
        budget.setUsed(0.0);
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(budget);
        } catch (Exception e) {
            return null;
        }
    }
} 