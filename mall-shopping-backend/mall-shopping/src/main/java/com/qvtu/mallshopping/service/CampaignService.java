package com.qvtu.mallshopping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvtu.mallshopping.dto.Budget;
import com.qvtu.mallshopping.dto.CampaignResponse;
import com.qvtu.mallshopping.dto.CampaignCreateRequest;
import com.qvtu.mallshopping.dto.CampaignUpdateRequest;
import com.qvtu.mallshopping.model.Campaign;
import com.qvtu.mallshopping.model.Promotion;
import com.qvtu.mallshopping.repository.CampaignRepository;
import com.qvtu.mallshopping.repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
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
        response.put("limit", size);
        response.put("offset", page * size);
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

    @Transactional
    public Campaign managePromotions(Long campaignId, List<String> addIds, List<String> removeIds) {
        log.info("管理活动促销, 活动ID: {}", campaignId);
        
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // 添加促销活动
        if (!addIds.isEmpty()) {
            log.info("添加促销活动到活动: {}", addIds);
            List<Promotion> promotionsToAdd = promotionRepository.findAllById(
                addIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList())
            );
            
            // 更新关联关系
            for (Promotion promotion : promotionsToAdd) {
                promotion.setCampaignId(campaignId.toString());
                promotionRepository.save(promotion);
            }
        }
        
        // 移除促销活动
        if (!removeIds.isEmpty()) {
            log.info("从活动中移除促销活动: {}", removeIds);
            List<Promotion> promotionsToRemove = promotionRepository.findAllById(
                removeIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList())
            );
            
            // 更新关联关系
            for (Promotion promotion : promotionsToRemove) {
                promotion.setCampaignId(null);
                promotionRepository.save(promotion);
            }
        }
        
        // 刷新活动数据
        campaign = campaignRepository.findById(campaignId).get();
        log.info("活动促销管理完成: {}", campaign);
        
        return campaign;
    }

    public Map<String, Object> getCampaignPromotions(Long campaignId, int page, int size) {
        log.info("获取活动的促销列表, 活动ID: {}, page: {}, size: {}", campaignId, page, size);
        
        // 验证活动是否存在
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // 获取该活动的所有促销活动
        Page<Promotion> promotions = promotionRepository.findByCampaignId(
            campaignId.toString(), 
            PageRequest.of(page, size)
        );
        
        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("promotions", promotions.getContent().stream()
            .map(this::formatPromotionResponse)
            .collect(Collectors.toList()));
        response.put("count", promotions.getTotalElements());
        response.put("limit", size);
        response.put("offset", page * size);
        
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
        return formatted;
    }
} 