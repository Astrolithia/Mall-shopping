package com.qvtu.mallshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDTO {
    private String id;
    private String description;
    private String type;
    private String attribute;
    private String operator;
    private List<String> values;
} 