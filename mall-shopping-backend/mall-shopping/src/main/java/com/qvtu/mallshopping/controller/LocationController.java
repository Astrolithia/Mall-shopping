package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.LocationResponseDTO;
import com.qvtu.mallshopping.model.Location;
import com.qvtu.mallshopping.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(@RequestBody Location location) {
        Location savedLocation = locationRepository.save(location);
        
        LocationResponseDTO response = new LocationResponseDTO();
        response.setId(savedLocation.getId());
        response.setName(savedLocation.getName());
        response.setAddress(savedLocation.getAddress());
        response.setCreatedAt(savedLocation.getCreatedAt());
        response.setUpdatedAt(savedLocation.getUpdatedAt());
        
        return ResponseEntity.ok(response);
    }
} 