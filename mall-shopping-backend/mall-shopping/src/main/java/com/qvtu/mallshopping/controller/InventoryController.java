package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.InventoryCreateRequest;
import com.qvtu.mallshopping.dto.InventoryResponseDTO;
import com.qvtu.mallshopping.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listInventories(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        try {
            Map<String, Object> result = inventoryService.listInventories(offset, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(@RequestBody InventoryCreateRequest request) {
        try {
            InventoryResponseDTO inventory = inventoryService.createInventory(request);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 