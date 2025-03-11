package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LocationResponseDTO {
    private Long id;
    private String name;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
} 