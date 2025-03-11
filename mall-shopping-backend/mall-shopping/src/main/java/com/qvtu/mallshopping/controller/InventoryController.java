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
import com.qvtu.mallshopping.model.InventoryLevel;
import com.qvtu.mallshopping.repository.InventoryLevelRepository;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    private final InventoryService inventoryService;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLevelRepository inventoryLevelRepository;

    @Autowired
    public InventoryController(
        InventoryService inventoryService,
        LocationRepository locationRepository,
        InventoryRepository inventoryRepository,
        InventoryLevelRepository inventoryLevelRepository
    ) {
        this.inventoryService = inventoryService;
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
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
            Inventory inventory = inventoryRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
            
            // 生成测试数据
            InventoryDataGenerator generator = new InventoryDataGenerator();
            List<Map<String, Object>> locationLevels = generator.generateLocationLevels(count);
            
            List<InventoryLevel> savedLevels = new ArrayList<>();
            
            // 为每个生成的数据创建库存水平记录
            for (Map<String, Object> level : locationLevels) {
                InventoryLevel inventoryLevel = new InventoryLevel();
                inventoryLevel.setInventory(inventory);
                inventoryLevel.setLocation(inventory.getLocation());
                inventoryLevel.setStockedQuantity((Integer) level.get("stocked_quantity"));
                inventoryLevel.setReservedQuantity((Integer) level.get("reserved_quantity"));
                inventoryLevel.setIncomingQuantity((Integer) level.get("incoming_quantity"));
                inventoryLevel.setMetadata((Map<String, Object>) level.get("metadata"));
                
                savedLevels.add(inventoryLevelRepository.save(inventoryLevel));
            }
            
            // 返回生成的数据
            Map<String, Object> response = new HashMap<>();
            response.put("location_levels", savedLevels.stream()
                .map(this::convertLevelToMap)
                .collect(Collectors.toList()));
            response.put("count", savedLevels.size());
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Inventory item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    private Map<String, Object> convertLevelToMap(InventoryLevel level) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", level.getId());
        result.put("location_id", level.getLocation().getId());
        result.put("stocked_quantity", level.getStockedQuantity());
        result.put("reserved_quantity", level.getReservedQuantity());
        result.put("available_quantity", level.getStockedQuantity() - level.getReservedQuantity());
        result.put("incoming_quantity", level.getIncomingQuantity());
        result.put("created_at", level.getCreatedAt());
        result.put("updated_at", level.getUpdatedAt());
        result.put("deleted_at", level.getDeletedAt());
        result.put("metadata", level.getMetadata());
        return result;
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