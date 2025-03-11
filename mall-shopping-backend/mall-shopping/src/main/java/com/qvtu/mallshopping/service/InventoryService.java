package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.InventoryResponseDTO;
import com.qvtu.mallshopping.dto.LocationResponseDTO;
import com.qvtu.mallshopping.model.Inventory;
import com.qvtu.mallshopping.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Page<InventoryResponseDTO> listInventoryItems(Integer offset, Integer limit) {
        // 创建分页请求
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        
        // 获取分页数据
        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageRequest);
        
        // 转换为 DTO
        return inventoryPage.map(this::convertToDTO);
    }

    private InventoryResponseDTO convertToDTO(Inventory inventory) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(inventory.getId());
        dto.setSku(inventory.getSku());
        dto.setQuantity(inventory.getQuantity());
        dto.setAllowBackorder(inventory.getAllowBackorder());
        dto.setManageInventory(inventory.getManageInventory());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        dto.setDeletedAt(inventory.getDeletedAt());
        
        // 如果有位置信息,转换位置信息
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