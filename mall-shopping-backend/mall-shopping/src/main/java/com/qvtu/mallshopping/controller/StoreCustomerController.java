package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.service.CustomerService;
import com.qvtu.mallshopping.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/store/customers")
public class StoreCustomerController {
    
    private final CustomerService customerService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public StoreCustomerController(CustomerService customerService, JwtTokenProvider jwtTokenProvider) {
        this.customerService = customerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestHeader(value = "x-publishable-api-key", required = true) String publishableApiKey,
            @RequestBody Map<String, Object> request) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从请求体中获取客户信息
            String email = (String) request.get("email");
            String companyName = (String) request.get("company_name");
            String firstName = (String) request.get("first_name");
            String lastName = (String) request.get("last_name");
            String phone = (String) request.get("phone");
            Map<String, Object> metadata = request.containsKey("metadata") ? 
                    (Map<String, Object>) request.get("metadata") : new HashMap<>();
            
            // 创建客户
            Customer customer = customerService.createStoreCustomer(email, companyName, firstName, lastName, phone, metadata);
            
            // 格式化响应
            Map<String, Object> customerData = formatCustomerResponse(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customerData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "创建客户失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * 格式化客户响应
     */
    private Map<String, Object> formatCustomerResponse(Customer customer) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", customer.getId().toString());
        formatted.put("email", customer.getEmail());
        
        if (customer.getDefaultBillingAddress() != null) {
            formatted.put("default_billing_address_id", customer.getDefaultBillingAddress().getId().toString());
        } else {
            formatted.put("default_billing_address_id", null);
        }
        
        if (customer.getDefaultShippingAddress() != null) {
            formatted.put("default_shipping_address_id", customer.getDefaultShippingAddress().getId().toString());
        } else {
            formatted.put("default_shipping_address_id", null);
        }
        
        formatted.put("company_name", customer.getCompanyName());
        formatted.put("first_name", customer.getFirstName());
        formatted.put("last_name", customer.getLastName());
        formatted.put("phone", customer.getPhone());
        formatted.put("metadata", customer.getMetadata());
        formatted.put("created_at", customer.getCreatedAt());
        formatted.put("updated_at", customer.getUpdatedAt());
        
        // 添加地址信息
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            formatted.put("addresses", customer.getAddresses().stream()
                    .map(address -> {
                        Map<String, Object> addressMap = new HashMap<>();
                        addressMap.put("id", address.getId().toString());
                        addressMap.put("address_name", address.getAddressName());
                        addressMap.put("is_default_shipping", address.isDefaultShipping());
                        addressMap.put("is_default_billing", address.isDefaultBilling());
                        addressMap.put("customer_id", address.getCustomer().getId().toString());
                        addressMap.put("company", address.getCompany());
                        addressMap.put("first_name", address.getFirstName());
                        addressMap.put("last_name", address.getLastName());
                        addressMap.put("address_1", address.getAddress1());
                        addressMap.put("address_2", address.getAddress2());
                        addressMap.put("city", address.getCity());
                        addressMap.put("country_code", address.getCountryCode());
                        addressMap.put("province", address.getProvince());
                        addressMap.put("postal_code", address.getPostalCode());
                        addressMap.put("phone", address.getPhone());
                        addressMap.put("metadata", address.getMetadata());
                        addressMap.put("created_at", address.getCreatedAt());
                        addressMap.put("updated_at", address.getUpdatedAt());
                        return addressMap;
                    })
                    .toList());
        } else {
            formatted.put("addresses", new java.util.ArrayList<>());
        }
        
        return formatted;
    }
} 