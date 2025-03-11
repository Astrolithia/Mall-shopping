package com.qvtu.mallshopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class LocationLevelBatchRequestDTO {
    @JsonProperty("location_levels")
    private List<LocationLevelDTO> locationLevels;
} 