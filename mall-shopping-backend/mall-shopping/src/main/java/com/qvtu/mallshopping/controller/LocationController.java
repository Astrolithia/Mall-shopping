package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.LocationResponseDTO;
import com.qvtu.mallshopping.dto.StockLocationCreateRequest;
import com.qvtu.mallshopping.dto.StockLocationResponseDTO;
import com.qvtu.mallshopping.dto.StockLocationUpdateRequest;
import com.qvtu.mallshopping.dto.FulfillmentProviderRequest;
import com.qvtu.mallshopping.dto.FulfillmentSetRequest;
import com.qvtu.mallshopping.dto.SalesChannelRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Location;
import com.qvtu.mallshopping.repository.LocationRepository;
import com.qvtu.mallshopping.service.LocationService;
import com.qvtu.mallshopping.util.LocationDataGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/stock-locations")
public class LocationController {
    private final LocationRepository locationRepository;
    private final LocationService locationService;

    public LocationController(LocationRepository locationRepository, LocationService locationService) {
        this.locationRepository = locationRepository;
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLocation(@RequestBody StockLocationCreateRequest request) {
        try {
            Map<String, Object> response = locationService.createLocation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listLocations(
        @RequestParam(required = false) Integer offset,
        @RequestParam(required = false) Integer limit
    ) {
        Map<String, Object> response = locationService.listLocations(offset, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLocation(@PathVariable Long id) {
        try {
            Map<String, Object> response = locationService.getLocation(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLocation(
        @PathVariable Long id,
        @RequestBody StockLocationUpdateRequest request
    ) {
        try {
            Map<String, Object> response = locationService.updateLocation(id, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/generate-test-data")
    public ResponseEntity<Map<String, Object>> generateTestData(
        @RequestParam(defaultValue = "5") Integer count
    ) {
        try {
            LocationDataGenerator generator = new LocationDataGenerator();
            List<Location> locations = generator.generateLocations(count);
            
            List<StockLocationResponseDTO> results = new ArrayList<>();
            for (Location location : locations) {
                Location savedLocation = locationRepository.save(location);
                StockLocationResponseDTO dto = locationService.convertToDTO(savedLocation);
                results.add(dto);
            }
            
            return ResponseEntity.ok(Collections.singletonMap("generated_locations", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteLocation(id);
            
            // 返回符合 Medusa Admin API 格式的响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", id.toString());
            response.put("object", "stock_location");
            response.put("deleted", true);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/fulfillment-providers")
    public ResponseEntity<Map<String, Object>> addFulfillmentProvider(
        @PathVariable Long id,
        @RequestBody FulfillmentProviderRequest request
    ) {
        try {
            if (request.getProviderId() == null || request.getProviderId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "provider_id is required"));
            }
            
            Map<String, Object> response = locationService.addFulfillmentProvider(id, request.getProviderId());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/fulfillment-sets")
    public ResponseEntity<Map<String, Object>> addFulfillmentSet(
        @PathVariable Long id,
        @RequestBody FulfillmentSetRequest request
    ) {
        try {
            // 验证请求参数
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "name is required"));
            }
            if (request.getType() == null || request.getType().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "type is required"));
            }
            
            Map<String, Object> response = locationService.addFulfillmentSet(id, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/sales-channels")
    public ResponseEntity<Map<String, Object>> addSalesChannel(
        @PathVariable Long id,
        @RequestBody SalesChannelRequest request
    ) {
        try {
            if (request.getSalesChannelId() == null || request.getSalesChannelId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "sales_channel_id is required"));
            }
            
            Map<String, Object> response = locationService.addSalesChannel(id, request.getSalesChannelId());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "Stock location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
} 