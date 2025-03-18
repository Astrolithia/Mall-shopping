package com.qvtu.mallshopping.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.qvtu.mallshopping.model.Customer;
import com.qvtu.mallshopping.model.Address;
import com.qvtu.mallshopping.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvtu.mallshopping.dto.CustomerCreateRequest;
import com.qvtu.mallshopping.repository.AddressRepository;
import java.util.ArrayList;

@Service
@Slf4j
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    public Map<String, Object> listCustomers(int page, int size) {
        log.info("获取客户列表, 页码: {}, 每页数量: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        
        List<Map<String, Object>> formattedCustomers = customerPage.getContent().stream()
            .map(this::formatCustomerResponse)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("customers", formattedCustomers);
        response.put("count", customerPage.getTotalElements());
        response.put("offset", page * size);
        response.put("limit", size);

        return response;
    }

    private Map<String, Object> formatCustomerResponse(Customer customer) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", customer.getId().toString());
        formatted.put("has_account", customer.getHasAccount());
        formatted.put("email", customer.getEmail());
        formatted.put("default_billing_address_id", customer.getDefaultBillingAddressId());
        formatted.put("default_shipping_address_id", customer.getDefaultShippingAddressId());
        formatted.put("company_name", customer.getCompanyName());
        formatted.put("first_name", customer.getFirstName());
        formatted.put("last_name", customer.getLastName());

        List<Map<String, Object>> formattedAddresses = customer.getAddresses().stream()
            .map(this::formatAddressResponse)
            .collect(Collectors.toList());
        formatted.put("addresses", formattedAddresses);

        return formatted;
    }

    private Map<String, Object> formatAddressResponse(Address address) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", address.getId().toString());
        formatted.put("address_name", address.getAddressName());
        formatted.put("is_default_shipping", address.getIsDefaultShipping());
        formatted.put("is_default_billing", address.getIsDefaultBilling());
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
        formatted.put("metadata", address.getMetadata());
        formatted.put("created_at", address.getCreatedAt());
        formatted.put("updated_at", address.getUpdatedAt());
        
        return formatted;
    }

    public Map<String, Object> createCustomer(CustomerCreateRequest request) {
        log.info("开始创建客户");
        
        // 检查邮箱是否已存在
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 创建新客户
        Customer customer = Customer.builder()
            .email(request.getEmail())
            .companyName(request.getCompanyName())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .hasAccount(true)
            .metadata(request.getMetadata())
            .addresses(new ArrayList<>()) // 初始化为可变列表
            .build();

        customer = customerRepository.save(customer);
        log.info("客户创建成功, ID: {}", customer.getId());

        // 如果提供了电话号码,创建默认地址
        if (request.getPhone() != null) {
            Address address = Address.builder()
                .customer(customer)
                .phone(request.getPhone())
                .isDefaultShipping(true)
                .isDefaultBilling(true)
                .metadata(new HashMap<>())
                .build();
            
            address = addressRepository.save(address);
            customer.getAddresses().add(address); // 使用 add 方法添加到列表
            customer.setDefaultBillingAddressId(address.getId().toString());
            customer.setDefaultShippingAddressId(address.getId().toString());
            customer = customerRepository.save(customer);
        }

        // 格式化响应
        Map<String, Object> response = new HashMap<>();
        response.put("customer", formatCustomerResponse(customer));
        
        return response;
    }
} 