package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.InventoryResponseDTO;
import com.qvtu.mallshopping.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listInventoryItems(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            Page<InventoryResponseDTO> page = inventoryService.listInventoryItems(offset, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("inventory_items", page.getContent());
            response.put("count", page.getTotalElements());
            response.put("offset", offset);
            response.put("limit", limit);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取库存列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 