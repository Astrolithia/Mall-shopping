package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.model.Location;
import com.qvtu.mallshopping.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import com.qvtu.mallshopping.dto.StockLocationResponseDTO;
import com.qvtu.mallshopping.dto.StockLocationCreateRequest;
import java.util.Collections;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.dto.StockLocationUpdateRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import com.qvtu.mallshopping.dto.FulfillmentSetRequest;

@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public void initializeDefaultLocations() {
        if (locationRepository.count() == 0) {
            Location defaultLocation = new Location();
            defaultLocation.setName("默认仓库");
            defaultLocation.setAddress("默认地址");
            defaultLocation.setCity("上海");
            defaultLocation.setCountryCode("CN");
            defaultLocation.setPostalCode("200120");
            defaultLocation.setProvince("上海");
            defaultLocation.setPhone("021-12345678");
            locationRepository.save(defaultLocation);

            Location store = new Location();
            store.setName("实体店");
            store.setAddress("浦东新区陆家嘴");
            store.setCity("上海");
            store.setCountryCode("CN");
            store.setPostalCode("200120");
            store.setProvince("上海");
            store.setPhone("021-87654321");
            locationRepository.save(store);
        }
    }

    public Map<String, Object> listLocations(Integer offset, Integer limit) {
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );
        
        Page<Location> locations = locationRepository.findByDeletedAtIsNull(pageRequest);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("stock_locations", locations.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
        response.put("count", locations.getTotalElements());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);
        
        return response;
    }

    public StockLocationResponseDTO convertToDTO(Location location) {
        StockLocationResponseDTO dto = new StockLocationResponseDTO();
        dto.setId(location.getId().toString());
        dto.setName(location.getName());
        dto.setAddress_id(location.getId().toString());
        return dto;
    }

    @Transactional
    public Map<String, Object> createLocation(StockLocationCreateRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());
        location.setCountryCode(request.getCountry_code());
        location.setPostalCode(request.getPostal_code());
        location.setPhone(request.getPhone());
        location.setProvince(request.getProvince());
        location.setMetadata(request.getMetadata());

        location = locationRepository.save(location);

        return Collections.singletonMap("stock_location", convertToDTO(location));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLocation(Long id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));

        return Collections.singletonMap("stock_location", convertToDTO(location));
    }

    @Transactional
    public Map<String, Object> updateLocation(Long id, StockLocationUpdateRequest request) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));

        if (request.getMetadata() != null) {
            location.setMetadata(request.getMetadata());
        }

        location = locationRepository.save(location);

        return Collections.singletonMap("stock_location", convertToDTO(location));
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));
        
        // 检查是否有关联的库存
        if (!location.getInventories().isEmpty()) {
            throw new IllegalStateException("Cannot delete location with associated inventory items");
        }
        
        // 软删除 - 设置删除时间
        location.setDeletedAt(LocalDateTime.now());
        locationRepository.save(location);
    }

    @Transactional
    public Map<String, Object> addFulfillmentProvider(Long id, String providerId) {
        // 查找库存位置
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));
        
        // 更新位置的元数据，添加流行提供商信息
        Map<String, Object> metadata = location.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        // 添加或更新提供商信息
        List<String> providers = (List<String>) metadata.getOrDefault("fulfillment_providers", new ArrayList<String>());
        if (!providers.contains(providerId)) {
            providers.add(providerId);
        }
        metadata.put("fulfillment_providers", providers);
        
        location.setMetadata(metadata);
        location = locationRepository.save(location);
        
        // 返回更新后的位置信息
        return Collections.singletonMap("stock_location", convertToDTO(location));
    }

    @Transactional
    public Map<String, Object> addFulfillmentSet(Long id, FulfillmentSetRequest request) {
        // 查找库存位置
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));
        
        // 更新位置的元数据，添加履行集信息
        Map<String, Object> metadata = location.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        // 添加或更新履行集信息
        List<Map<String, Object>> fulfillmentSets = 
            (List<Map<String, Object>>) metadata.getOrDefault("fulfillment_sets", new ArrayList<>());
        
        // 创建新的履行集
        Map<String, Object> newSet = new HashMap<>();
        newSet.put("name", request.getName());
        newSet.put("type", request.getType());
        fulfillmentSets.add(newSet);
        
        metadata.put("fulfillment_sets", fulfillmentSets);
        location.setMetadata(metadata);
        
        // 保存更新
        location = locationRepository.save(location);
        
        // 返回更新后的位置信息
        return Collections.singletonMap("stock_location", convertToDTO(location));
    }

    @Transactional
    public Map<String, Object> addSalesChannel(Long id, String salesChannelId) {
        // 查找库存位置
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock location not found"));
        
        // 更新位置的元数据，添加销售渠道信息
        Map<String, Object> metadata = location.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        // 添加或更新销售渠道信息
        List<String> salesChannels = (List<String>) metadata.getOrDefault("sales_channels", new ArrayList<String>());
        if (!salesChannels.contains(salesChannelId)) {
            salesChannels.add(salesChannelId);
        }
        metadata.put("sales_channels", salesChannels);
        
        location.setMetadata(metadata);
        location = locationRepository.save(location);
        
        // 返回更新后的位置信息
        return Collections.singletonMap("stock_location", convertToDTO(location));
    }
} 