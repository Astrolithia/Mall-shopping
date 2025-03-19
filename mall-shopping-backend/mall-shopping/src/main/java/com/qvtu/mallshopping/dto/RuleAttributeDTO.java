package com.qvtu.mallshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleAttributeDTO {
    private String id;
    private String name;
    private String description;
    private String type;
} 