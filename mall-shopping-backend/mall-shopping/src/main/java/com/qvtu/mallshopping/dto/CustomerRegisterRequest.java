package com.qvtu.mallshopping.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;
    
    private String phone;
} 