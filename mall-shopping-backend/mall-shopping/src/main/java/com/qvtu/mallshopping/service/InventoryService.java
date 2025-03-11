package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.InventoryCreateRequest;
import com.qvtu.mallshopping.dto.InventoryResponseDTO;
import com.qvtu.mallshopping.dto.LocationLevelDTO;
import com.qvtu.mallshopping.dto.LocationResponseDTO;
import com.qvtu.mallshopping.dto.UpdateInventoryItemDTO;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Inventory;
import com.qvtu.mallshopping.model.Location;
import com.qvtu.mallshopping.repository.InventoryRepository;
import com.qvtu.mallshopping.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, LocationRepository locationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public InventoryResponseDTO createInventory(InventoryCreateRequest request) {
        // 查找库存位置
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        // 创建新的库存记录
        Inventory inventory = new Inventory();
        inventory.setSku(request.getSku());
        inventory.setQuantity(request.getQuantity());
        inventory.setAllowBackorder(request.getAllowBackorder());
        inventory.setManageInventory(request.getManageInventory());
        inventory.setLocation(location);

        // 保存库存记录
        inventory = inventoryRepository.save(inventory);

        // 转换为响应DTO
        return convertToDTO(inventory);
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

        // 转换位置信息
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

    public Map<String, Object> listInventories(Integer offset, Integer limit) {
        // 创建分页请求
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );
        
        // 只获取未删除的记录
        Page<Inventory> inventoryPage = inventoryRepository.findByDeletedAtIsNull(pageRequest);
        
        List<Map<String, Object>> items = inventoryPage.getContent()
            .stream()
            .map(inventory -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", inventory.getId().toString());
                item.put("sku", inventory.getSku());
                item.put("height", inventory.getHeight());
                item.put("width", inventory.getWidth());
                item.put("length", inventory.getLength());
                item.put("weight", inventory.getWeight());
                item.put("mid_code", inventory.getMidCode());
                item.put("hs_code", inventory.getHsCode());
                item.put("origin_country", inventory.getOriginCountry());
                item.put("quantity", inventory.getQuantity());
                item.put("allowBackorder", inventory.getAllowBackorder());
                item.put("manageInventory", inventory.getManageInventory());
                item.put("metadata", inventory.getMetadata() != null ? inventory.getMetadata() : new HashMap<>());
                item.put("createdAt", inventory.getCreatedAt());
                item.put("updatedAt", inventory.getUpdatedAt());
                
                if (inventory.getLocation() != null) {
                    Map<String, Object> location = new HashMap<>();
                    location.put("id", inventory.getLocation().getId());
                    location.put("name", inventory.getLocation().getName());
                    item.put("location", location);
                }
                
                return item;
            })
            .collect(Collectors.toList());
        
        // 构造符合 Medusa 格式的响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("inventory_items", items);
        response.put("count", inventoryPage.getTotalElements());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);
        
        return response;
    }

    @Transactional
    public List<Map<String, Object>> updateLocationLevels(List<LocationLevelDTO> locationLevels) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (LocationLevelDTO level : locationLevels) {
            try {
                // 查找库存项目
                Inventory inventory = inventoryRepository.findById(Long.parseLong(level.getInventoryItemId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
                
                // 查找位置
                Location location = locationRepository.findById(level.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
                
                // 更新库存数量
                inventory.setQuantity(level.getStockedQuantity());
                inventory.setLocation(location);
                
                // 保存更新
                inventory = inventoryRepository.save(inventory);
                
                // 构造响应
                Map<String, Object> result = new HashMap<>();
                result.put("id", inventory.getId().toString());
                result.put("inventory_item_id", inventory.getId().toString());
                result.put("location_id", location.getId());
                result.put("stocked_quantity", inventory.getQuantity());
                result.put("incoming_quantity", level.getIncomingQuantity());
                result.put("available_quantity", inventory.getQuantity());
                result.put("created_at", inventory.getCreatedAt());
                result.put("updated_at", inventory.getUpdatedAt());
                result.put("deleted_at", inventory.getDeletedAt());
                result.put("metadata", inventory.getMetadata());
                
                results.add(result);
            } catch (Exception e) {
                // 使用注入的 log 变量记录错误
                log.error("Error updating location level for inventory item {}: {}", 
                    level.getInventoryItemId(), e.getMessage());
            }
        }
        
        return results;
    }

    public Map<String, Object> getInventoryItem(String id) {
        Inventory inventory = inventoryRepository.findByIdAndDeletedAtIsNull(Long.parseLong(id))
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", inventory.getId().toString());
        result.put("sku", inventory.getSku());
        result.put("height", inventory.getHeight());
        result.put("width", inventory.getWidth());
        result.put("length", inventory.getLength());
        result.put("weight", inventory.getWeight());
        result.put("mid_code", inventory.getMidCode());
        result.put("hs_code", inventory.getHsCode());
        result.put("origin_country", inventory.getOriginCountry());
        result.put("requires_shipping", true);
        result.put("metadata", inventory.getMetadata() != null ? inventory.getMetadata() : new HashMap<>());
        result.put("created_at", inventory.getCreatedAt());
        result.put("updated_at", inventory.getUpdatedAt());
        result.put("deleted_at", inventory.getDeletedAt());

        // 添加库存水平信息
        if (inventory.getLocation() != null) {
            List<Map<String, Object>> locationLevels = new ArrayList<>();
            Map<String, Object> locationLevel = new HashMap<>();
            locationLevel.put("location_id", inventory.getLocation().getId());
            locationLevel.put("stocked_quantity", inventory.getQuantity());
            locationLevel.put("reserved_quantity", 0); // 如果有预留数量，这里需要修改
            locationLevel.put("available_quantity", inventory.getQuantity());
            locationLevel.put("incoming_quantity", 0); // 如果有进货数量，这里需要修改
            locationLevels.add(locationLevel);
            result.put("location_levels", locationLevels);
        }

        return result;
    }

    @Transactional
    public Map<String, Object> updateInventoryItem(String id, UpdateInventoryItemDTO updateData) {
        // 查找库存项目
        Inventory inventory = inventoryRepository.findById(Long.parseLong(id))
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        // 更新字段
        if (updateData.getSku() != null) {
            inventory.setSku(updateData.getSku());
        }
        if (updateData.getHeight() != null) {
            inventory.setHeight(updateData.getHeight());
        }
        if (updateData.getWidth() != null) {
            inventory.setWidth(updateData.getWidth());
        }
        if (updateData.getLength() != null) {
            inventory.setLength(updateData.getLength());
        }
        if (updateData.getWeight() != null) {
            inventory.setWeight(updateData.getWeight());
        }
        if (updateData.getMidCode() != null) {
            inventory.setMidCode(updateData.getMidCode());
        }
        if (updateData.getHsCode() != null) {
            inventory.setHsCode(updateData.getHsCode());
        }
        if (updateData.getOriginCountry() != null) {
            inventory.setOriginCountry(updateData.getOriginCountry());
        }
        
        // 更新元数据
        if (updateData.getMetadata() != null) {
            Map<String, Object> currentMetadata = inventory.getMetadata();
            if (currentMetadata == null) {
                currentMetadata = new HashMap<>();
            }
            currentMetadata.putAll(updateData.getMetadata());
            inventory.setMetadata(currentMetadata);
        }

        // 保存更新
        inventory = inventoryRepository.save(inventory);

        // 构造简化的响应
        Map<String, Object> result = new HashMap<>();
        result.put("id", inventory.getId().toString());
        result.put("requires_shipping", true);

        return result;
    }

    @Transactional
    public void deleteInventoryItem(String id) {
        // 查找库存项目
        Inventory inventory = inventoryRepository.findById(Long.parseLong(id))
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        // 软删除 - 设置删除时间
        inventory.setDeletedAt(LocalDateTime.now());
        
        // 保存更新
        inventoryRepository.save(inventory);
    }

    public Map<String, Object> getInventoryItemLocationLevels(String id, Integer offset, Integer limit) {
        // 查找库存项目
        Inventory inventory = inventoryRepository.findByIdAndDeletedAtIsNull(Long.parseLong(id))
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        // 创建分页请求
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );

        // 获取库存水平列表
        List<Map<String, Object>> locationLevels = new ArrayList<>();
        if (inventory.getLocation() != null) {
            Map<String, Object> locationLevel = new HashMap<>();
            locationLevel.put("location_id", inventory.getLocation().getId());
            locationLevel.put("stocked_quantity", inventory.getQuantity());
            locationLevel.put("reserved_quantity", 0); // 如果有预留数量，这里需要修改
            locationLevel.put("available_quantity", inventory.getQuantity());
            locationLevel.put("incoming_quantity", 0); // 如果有进货数量，这里需要修改
            locationLevel.put("created_at", inventory.getCreatedAt());
            locationLevel.put("updated_at", inventory.getUpdatedAt());
            locationLevel.put("deleted_at", inventory.getDeletedAt());
            locationLevel.put("metadata", inventory.getMetadata() != null ? inventory.getMetadata() : new HashMap<>());
            locationLevels.add(locationLevel);
        }

        // 构造符合 Medusa 格式的响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("location_levels", locationLevels);
        response.put("count", locationLevels.size());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);

        return response;
    }
} 