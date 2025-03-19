package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class RuleBatchRequestDTO {
    private List<RuleDTO> rules;
} 