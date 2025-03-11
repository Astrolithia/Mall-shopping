package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import java.util.ArrayList;
import com.qvtu.mallshopping.util.InventoryDataGenerator;
import com.qvtu.mallshopping.model.Inventory;
import com.qvtu.mallshopping.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.qvtu.mallshopping.repository.InventoryRepository;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    private final InventoryService inventoryService;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryController(
        InventoryService inventoryService,
        LocationRepository locationRepository,
        InventoryRepository inventoryRepository
    ) {
        this.inventoryService = inventoryService;
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
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

    @PostMapping("/location-levels/batch")
    public ResponseEntity<Map<String, Object>> updateLocationLevels(
            @RequestBody LocationLevelBatchRequestDTO request) {
        try {
            List<Map<String, Object>> results = inventoryService.updateLocationLevels(request.getLocationLevels());
            
            Map<String, Object> response = new HashMap<>();
            response.put("inventory_items", results);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInventoryItem(@PathVariable String id) {
        try {
            Map<String, Object> result = inventoryService.getInventoryItem(id);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Inventory item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInventoryItem(
            @PathVariable String id,
            @RequestBody UpdateInventoryItemDTO updateData) {
        try {
            Map<String, Object> result = inventoryService.updateInventoryItem(id, updateData);
            return ResponseEntity.ok(Collections.singletonMap("inventory_item", result));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Inventory item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String id) {
        try {
            inventoryService.deleteInventoryItem(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate-test-data")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "5") Integer count) {
        try {
            InventoryDataGenerator generator = new InventoryDataGenerator();
            List<Inventory> inventories = generator.generateInventories(count);
            
            List<InventoryResponseDTO> results = new ArrayList<>();
            for (Inventory inventory : inventories) {
                inventory.setLocation(locationRepository.findById(1L).orElseThrow());
                results.add(convertToDTO(inventoryRepository.save(inventory)));
            }
            
            return ResponseEntity.ok(Collections.singletonMap("generated_items", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/location-levels")
    public ResponseEntity<Map<String, Object>> getInventoryItemLocationLevels(
        @PathVariable String id,
        @RequestParam(required = false) Integer offset,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            Map<String, Object> result = inventoryService.getInventoryItemLocationLevels(id, offset, limit);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Inventory item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/location-levels/generate")
    public ResponseEntity<Map<String, Object>> generateLocationLevelsTestData(
        @PathVariable String id,
        @RequestParam(defaultValue = "1") Integer count
    ) {
        try {
            // 首先检查库存项目是否存在
            inventoryService.getInventoryItem(id);
            
            // 生成测试数据
            InventoryDataGenerator generator = new InventoryDataGenerator();
            List<Map<String, Object>> locationLevels = generator.generateLocationLevels(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("location_levels", locationLevels);
            response.put("count", locationLevels.size());
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Inventory item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    private InventoryResponseDTO convertToDTO(Inventory inventory) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(inventory.getId().toString());
        dto.setSku(inventory.getSku());
        dto.setQuantity(inventory.getQuantity());
        dto.setAllowBackorder(inventory.getAllowBackorder());
        dto.setManageInventory(inventory.getManageInventory());
        dto.setRequiresShipping(true);
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        dto.setDeletedAt(inventory.getDeletedAt());

        if (inventory.getLocation() != null) {
            LocationResponseDTO locationDTO = new LocationResponseDTO();
            locationDTO.setId(inventory.getLocation().getId());
            locationDTO.setName(inventory.getLocation().getName());
            locationDTO.setAddress(inventory.getLocation().getAddress());
            locationDTO.setCreatedAt(inventory.getLocation().getCreatedAt());
            locationDTO.setUpdatedAt(inventory.getLocation().getUpdatedAt());
            locationDTO.setDeletedAt(inventory.getLocation().getDeletedAt());
            dto.setLocation(locationDTO);
        }

        return dto;
    }
} 