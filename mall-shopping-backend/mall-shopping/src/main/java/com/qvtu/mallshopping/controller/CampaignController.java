package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CampaignCreateRequest;
import com.qvtu.mallshopping.dto.CampaignUpdateRequest;
import com.qvtu.mallshopping.model.Campaign;
import com.qvtu.mallshopping.repository.CampaignRepository;
import com.qvtu.mallshopping.service.CampaignService;
import com.qvtu.mallshopping.util.CampaignDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    @Autowired
    private CampaignService campaignService;
    
    @Autowired
    private CampaignRepository campaignRepository;

    @GetMapping
    public Map<String, Object> getCampaigns(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取活动列表请求, page: {}, size: {}", page, size);
        return campaignService.getCampaigns(q, page, size);
    }

    @PostMapping
    public Map<String, Object> createCampaign(@RequestBody CampaignCreateRequest request) {
        log.info("收到创建活动请求: {}", request);
        
        Campaign campaign = campaignService.createCampaign(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("campaign", campaignService.formatCampaignResponse(campaign));
        
        return response;
    }

    @PostMapping("/generate-test-data")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "5") Integer count) {
        try {
            CampaignDataGenerator generator = new CampaignDataGenerator();
            List<Campaign> campaigns = generator.generateCampaigns(count);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Campaign campaign : campaigns) {
                results.add(campaignService.formatCampaignResponse(
                    campaignRepository.save(campaign)
                ));
            }
            
            return ResponseEntity.ok(Collections.singletonMap("generated_items", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCampaign(@PathVariable Long id) {
        try {
            Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaign", campaignService.formatCampaignResponse(campaign));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCampaign(
            @PathVariable Long id,
            @RequestBody CampaignUpdateRequest request) {
        try {
            Campaign campaign = campaignService.updateCampaign(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaign", campaignService.formatCampaignResponse(campaign));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCampaign(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id.toString());
            response.put("object", "campaign");
            response.put("deleted", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/promotions")
    public ResponseEntity<Map<String, Object>> managePromotions(
        @PathVariable Long id,
        @RequestBody Map<String, List<String>> request
    ) {
        log.info("收到管理活动促销请求, 活动ID: {}, 请求数据: {}", id, request);
        try {
            List<String> addIds = request.getOrDefault("add", new ArrayList<>());
            List<String> removeIds = request.getOrDefault("remove", new ArrayList<>());
            
            log.info("添加促销IDs: {}, 移除促销IDs: {}", addIds, removeIds);
            
            Campaign campaign = campaignService.managePromotions(id, addIds, removeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaign", campaignService.formatCampaignResponse(campaign));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("管理活动促销失败", e);
            if (e.getMessage().equals("Campaign not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/promotions")
    public ResponseEntity<Map<String, Object>> getCampaignPromotions(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("获取活动的促销列表, 活动ID: {}", id);
        try {
            Map<String, Object> response = campaignService.getCampaignPromotions(id, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取活动促销列表失败", e);
            if (e.getMessage().equals("Campaign not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
} 