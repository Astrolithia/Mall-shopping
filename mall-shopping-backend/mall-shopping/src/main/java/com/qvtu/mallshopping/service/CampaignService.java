package com.qvtu.mallshopping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvtu.mallshopping.dto.Budget;
import com.qvtu.mallshopping.dto.CampaignResponse;
import com.qvtu.mallshopping.dto.CampaignCreateRequest;
import com.qvtu.mallshopping.dto.CampaignUpdateRequest;
import com.qvtu.mallshopping.model.Campaign;
import com.qvtu.mallshopping.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> getCampaigns(String q, int page, int size) {
        log.info("获取活动列表, 页码: {}, 每页数量: {}", page, size);
        
        Page<Campaign> campaignPage;
        if (q != null && !q.trim().isEmpty()) {
            campaignPage = campaignRepository.findByNameContainingAndDeletedAtIsNull(q.trim(), PageRequest.of(page, size));
        } else {
            campaignPage = campaignRepository.findByDeletedAtIsNull(PageRequest.of(page, size));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("campaigns", campaignPage.getContent().stream()
            .map(this::formatCampaignResponse)
            .toList());
        response.put("count", campaignPage.getTotalElements());
        
        return response;
    }

    public Map<String, Object> formatCampaignResponse(Campaign campaign) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", campaign.getId().toString());
        formatted.put("name", campaign.getName());
        formatted.put("description", campaign.getDescription());
        formatted.put("currency", campaign.getCurrency());
        formatted.put("campaign_identifier", campaign.getCampaignIdentifier());
        formatted.put("starts_at", campaign.getStartsAt());
        formatted.put("ends_at", campaign.getEndsAt());
        formatted.put("created_at", campaign.getCreatedAt());
        formatted.put("updated_at", campaign.getUpdatedAt());
        formatted.put("deleted_at", campaign.getDeletedAt());

        try {
            if (campaign.getBudget() != null) {
                formatted.put("budget", objectMapper.readValue(campaign.getBudget(), Budget.class));
            }
        } catch (Exception e) {
            log.error("解析活动预算失败", e);
        }

        return formatted;
    }

    public Campaign createCampaign(CampaignCreateRequest request) {
        log.info("创建活动: {}", request);
        
        Campaign campaign = Campaign.builder()
            .name(request.getName())
            .campaignIdentifier(request.getCampaignIdentifier())
            .description(request.getDescription())
            .startsAt(request.getStartsAt())
            .endsAt(request.getEndsAt())
            .currency("CNY")  // 默认使用人民币
            .build();
            
        try {
            if (request.getBudget() != null) {
                String budgetJson = objectMapper.writeValueAsString(request.getBudget());
                // 确保 JSON 格式正确
                objectMapper.readTree(budgetJson);
                campaign.setBudget(budgetJson);
            }
        } catch (Exception e) {
            log.error("序列化预算数据失败", e);
            throw new RuntimeException("预算数据格式不正确", e);
        }
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("活动创建成功: {}", savedCampaign);
        
        return savedCampaign;
    }

    public Campaign updateCampaign(Long id, CampaignUpdateRequest request) {
        log.info("更新活动: {}, 数据: {}", id, request);
        
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // 只更新非空字段
        if (request.getName() != null) {
            campaign.setName(request.getName());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
        }
        if (request.getCampaignIdentifier() != null) {
            campaign.setCampaignIdentifier(request.getCampaignIdentifier());
        }
        if (request.getCurrency() != null) {
            campaign.setCurrency(request.getCurrency());
        }
        if (request.getStartsAt() != null) {
            campaign.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            campaign.setEndsAt(request.getEndsAt());
        }
        
        try {
            if (request.getBudget() != null) {
                String budgetJson = objectMapper.writeValueAsString(request.getBudget());
                // 确保 JSON 格式正确
                objectMapper.readTree(budgetJson);
                campaign.setBudget(budgetJson);
            }
        } catch (Exception e) {
            log.error("序列化预算数据失败", e);
            throw new RuntimeException("预算数据格式不正确", e);
        }
        
        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("活动更新成功: {}", updatedCampaign);
        
        return updatedCampaign;
    }

    public Campaign deleteCampaign(Long id) {
        log.info("删除活动: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        campaign.setDeletedAt(LocalDateTime.now());
        Campaign deletedCampaign = campaignRepository.save(campaign);
        
        log.info("活动删除成功: {}", deletedCampaign);
        
        return deletedCampaign;
    }
} 