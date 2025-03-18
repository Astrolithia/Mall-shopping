package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CustomerCreateRequest {
    private String email;
    private String companyName;
    private String firstName;
    private String lastName;
    private String phone;
    private Map<String, Object> metadata;
} 