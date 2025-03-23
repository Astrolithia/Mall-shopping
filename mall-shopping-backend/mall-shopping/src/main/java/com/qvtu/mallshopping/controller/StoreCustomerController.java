package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.model.Address;
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

    /**
     * 获取当前登录客户信息
     * 相关文档: https://docs.medusajs.com/api/store#customers_getcustomersme
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentCustomer(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 获取客户信息
            Customer customer = customerService.getCustomerById(customerId);
            
            // 格式化响应
            Map<String, Object> customerData = formatCustomerResponse(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customerData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "获取客户信息失败");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * 更新当前登录客户信息
     * 相关文档: https://docs.medusajs.com/api/store#customers_postcustomersme
     */
    @PostMapping("/me")
    public ResponseEntity<Map<String, Object>> updateCurrentCustomer(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey,
            @RequestBody Map<String, Object> request) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 从请求体中获取更新信息
            String companyName = (String) request.get("company_name");
            String firstName = (String) request.get("first_name");
            String lastName = (String) request.get("last_name");
            String phone = (String) request.get("phone");
            Map<String, Object> metadata = request.containsKey("metadata") ? 
                    (Map<String, Object>) request.get("metadata") : null;
            
            // 更新客户信息
            Customer customer = customerService.updateCustomerInfo(
                    customerId, companyName, firstName, lastName, phone, metadata);
            
            // 格式化响应
            Map<String, Object> customerData = formatCustomerResponse(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customerData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "更新客户信息失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 获取当前登录客户的地址列表
     * 相关文档: https://docs.medusajs.com/api/store#customers_getcustomersmeaddresses
     */
    @GetMapping("/me/addresses")
    public ResponseEntity<Map<String, Object>> getCustomerAddresses(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 获取客户地址列表
            Map<String, Object> result = customerService.getCustomerAddresses(customerId, limit, offset);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "获取地址列表失败");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * 为当前登录客户添加新地址
     * 相关文档: https://docs.medusajs.com/api/store#customers_postcustomersmeaddresses
     */
    @PostMapping("/me/addresses")
    public ResponseEntity<Map<String, Object>> addCustomerAddress(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey,
            @RequestBody Map<String, Object> request) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 从请求体中获取地址信息
            String firstName = (String) request.get("first_name");
            String lastName = (String) request.get("last_name");
            String phone = (String) request.get("phone");
            String company = (String) request.get("company");
            String address1 = (String) request.get("address_1");
            String address2 = (String) request.get("address_2");
            String city = (String) request.get("city");
            String countryCode = (String) request.get("country_code");
            String province = (String) request.get("province");
            String postalCode = (String) request.get("postal_code");
            String addressName = (String) request.get("address_name");
            Map<String, Object> metadata = request.containsKey("metadata") ? 
                    (Map<String, Object>) request.get("metadata") : null;
            
            // 创建新地址
            Customer customer = customerService.addCustomerAddress(
                    customerId, firstName, lastName, phone, company,
                    address1, address2, city, countryCode,
                    province, postalCode, addressName, metadata);
            
            // 格式化响应
            Map<String, Object> customerData = formatCustomerResponse(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customerData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "添加地址失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 获取当前登录客户的特定地址详情
     * 相关文档: https://docs.medusajs.com/api/store#customers_getcustomersmeaddressesaddress_id
     */
    @GetMapping("/me/addresses/{address_id}")
    public ResponseEntity<Map<String, Object>> getCustomerAddress(
            @PathVariable("address_id") Long addressId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 获取客户特定地址
            Address address = customerService.getCustomerAddress(customerId, addressId);
            
            // 格式化响应
            Map<String, Object> addressData = formatAddressResponse(address);
            Map<String, Object> response = new HashMap<>();
            response.put("address", addressData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "获取地址详情失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 更新当前登录客户的特定地址
     * 相关文档: https://docs.medusajs.com/api/store#customers_postcustomersmeaddressesaddress_id
     */
    @PostMapping("/me/addresses/{address_id}")
    public ResponseEntity<Map<String, Object>> updateCustomerAddress(
            @PathVariable("address_id") Long addressId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey,
            @RequestBody Map<String, Object> request) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 从请求体中获取要更新的地址字段
            String firstName = request.containsKey("first_name") ? (String) request.get("first_name") : null;
            String lastName = request.containsKey("last_name") ? (String) request.get("last_name") : null;
            String phone = request.containsKey("phone") ? (String) request.get("phone") : null;
            String company = request.containsKey("company") ? (String) request.get("company") : null;
            String address1 = request.containsKey("address_1") ? (String) request.get("address_1") : null;
            String address2 = request.containsKey("address_2") ? (String) request.get("address_2") : null;
            String city = request.containsKey("city") ? (String) request.get("city") : null;
            String countryCode = request.containsKey("country_code") ? (String) request.get("country_code") : null;
            String province = request.containsKey("province") ? (String) request.get("province") : null;
            String postalCode = request.containsKey("postal_code") ? (String) request.get("postal_code") : null;
            String addressName = request.containsKey("address_name") ? (String) request.get("address_name") : null;
            Map<String, Object> metadata = request.containsKey("metadata") ? 
                    (Map<String, Object>) request.get("metadata") : null;
            
            // 获取默认地址设置
            Boolean isDefaultShipping = request.containsKey("is_default_shipping") ? 
                    (Boolean) request.get("is_default_shipping") : null;
            Boolean isDefaultBilling = request.containsKey("is_default_billing") ? 
                    (Boolean) request.get("is_default_billing") : null;
            
            // 更新地址
            Customer customer = customerService.updateCustomerAddress(
                    customerId, addressId, firstName, lastName, phone, company,
                    address1, address2, city, countryCode, province, postalCode,
                    addressName, metadata, isDefaultShipping, isDefaultBilling);
            
            // 格式化响应
            Map<String, Object> customerData = formatCustomerResponse(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customerData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "更新地址失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 删除当前登录客户的特定地址
     * 相关文档: https://docs.medusajs.com/api/store#customers_deletecustomersmeaddressesaddress_id
     */
    @DeleteMapping("/me/addresses/{address_id}")
    public ResponseEntity<Map<String, Object>> deleteCustomerAddress(
            @PathVariable("address_id") Long addressId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("x-publishable-api-key") String publishableApiKey) {
        
        try {
            // 验证publishable API key
            // TODO: 添加publishableApiKey的验证逻辑
            
            // 从Authorization头中提取令牌
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = bearerToken.substring(7);
            
            // 验证JWT令牌
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 从令牌中获取用户ID
            Long customerId = jwtTokenProvider.getUserIdFromJWT(token);
            
            // 删除地址
            Long deletedAddressId = customerService.deleteCustomerAddress(customerId, addressId);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", deletedAddressId.toString());
            response.put("object", "address");
            response.put("deleted", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "删除地址失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 格式化地址响应
     * 
     * @param address 地址对象
     * @return 格式化后的地址数据
     */
    private Map<String, Object> formatAddressResponse(Address address) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", address.getId().toString());
        formatted.put("customer_id", address.getCustomer().getId().toString());
        formatted.put("company", address.getCompany());
        formatted.put("first_name", address.getFirstName());
        formatted.put("last_name", address.getLastName());
        formatted.put("address_1", address.getAddress1());
        formatted.put("address_2", address.getAddress2());
        formatted.put("city", address.getCity());
        formatted.put("country_code", address.getCountryCode());
        formatted.put("province", address.getProvince());
        formatted.put("postal_code", address.getPostalCode());
        formatted.put("phone", address.getPhone());
        formatted.put("address_name", address.getAddressName());
        formatted.put("is_default_shipping", address.isDefaultShipping());
        formatted.put("is_default_billing", address.isDefaultBilling());
        formatted.put("metadata", address.getMetadata());
        formatted.put("created_at", address.getCreatedAt());
        formatted.put("updated_at", address.getUpdatedAt());
        
        return formatted;
    }
} 