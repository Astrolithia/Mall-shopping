package com.qvtu.mallshopping.dto;

import lombok.Data;

@Data
public class CustomerAuthRequest {
    private String email;
    private String password;
} 