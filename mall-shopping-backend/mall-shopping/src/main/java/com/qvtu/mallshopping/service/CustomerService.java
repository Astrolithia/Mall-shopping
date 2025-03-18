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
import com.qvtu.mallshopping.dto.CustomerUpdateRequest;
import com.github.javafaker.Faker;
import java.util.Locale;
import java.time.LocalDate;
import jakarta.persistence.EntityManager;

@Service
@Slf4j
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager entityManager;

    public Map<String, Object> listCustomers(int page, int size) {
        log.info("获取客户列表, 页码: {}, 每页数量: {}", page, size);
        
        // 清除一级缓存
        entityManager.clear();
        
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
        log.info("开始创建客户, 请求数据: {}", request);
        
        try {
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
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .addresses(new ArrayList<>())
                .build();

            customer = customerRepository.save(customer);
            log.info("客户基本信息创建成功, ID: {}", customer.getId());

            // 如果提供了电话号码,创建默认地址
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                Address address = Address.builder()
                    .customer(customer)
                    .addressName("默认地址")
                    .isDefaultShipping(true)
                    .isDefaultBilling(true)
                    .company(customer.getCompanyName())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phone(request.getPhone())
                    .metadata(new HashMap<>())
                    .build();
                
                address = addressRepository.save(address);
                log.info("客户默认地址创建成功, ID: {}", address.getId());

                customer.getAddresses().add(address);
                customer.setDefaultBillingAddressId(address.getId().toString());
                customer.setDefaultShippingAddressId(address.getId().toString());
                customer = customerRepository.save(customer);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("customer", formatCustomerResponse(customer));
            
            log.info("客户创建完成, ID: {}", customer.getId());
            return response;
        } catch (Exception e) {
            log.error("创建客户失败", e);
            throw e;
        }
    }

    public Map<String, Object> getCustomer(Long id) {
        log.info("获取客户详情, ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("customer", formatCustomerResponse(customer));
        
        return response;
    }

    public Map<String, Object> updateCustomer(Long id, CustomerUpdateRequest request) {
        log.info("开始更新客户信息, ID: {}, 请求数据: {}", id, request);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 如果要更新邮箱，检查新邮箱是否已被使用
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(c -> {
                if (!c.getId().equals(id)) {  // 添加ID检查，允许更新自己的邮箱
                    throw new RuntimeException("Email already exists");
                }
            });
            customer.setEmail(request.getEmail());
        }

        // 更新基本信息
        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getMetadata() != null) {
            customer.setMetadata(request.getMetadata());
        }

        // 如果提供了新的电话号码，更新或创建默认地址
        if (request.getPhone() != null) {
            Address defaultAddress = customer.getAddresses().stream()
                .filter(addr -> Boolean.TRUE.equals(addr.getIsDefaultBilling()))
                .findFirst()
                .orElse(Address.builder()
                    .customer(customer)
                    .isDefaultShipping(true)
                    .isDefaultBilling(true)
                    .metadata(new HashMap<>())
                    .build());

            defaultAddress.setPhone(request.getPhone());
            // 同步更新地址的其他信息
            defaultAddress.setFirstName(customer.getFirstName());
            defaultAddress.setLastName(customer.getLastName());
            defaultAddress.setCompany(customer.getCompanyName());
            
            defaultAddress = addressRepository.save(defaultAddress);

            if (!customer.getAddresses().contains(defaultAddress)) {
                customer.getAddresses().add(defaultAddress);
                customer.setDefaultBillingAddressId(defaultAddress.getId().toString());
                customer.setDefaultShippingAddressId(defaultAddress.getId().toString());
            }
        }

        customer = customerRepository.save(customer);
        log.info("客户信息更新成功, ID: {}", customer.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("customer", formatCustomerResponse(customer));
        
        return response;
    }

    public Map<String, Object> deleteCustomer(Long id) {
        log.info("开始删除客户, ID: {}", id);
        
        // 检查客户是否存在
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 删除客户
        customerRepository.delete(customer);
        log.info("客户删除成功, ID: {}", id);

        // 格式化响应
        Map<String, Object> response = new HashMap<>();
        response.put("id", id.toString());
        response.put("object", "customer");
        response.put("deleted", true);
        
        return response;
    }

    public Map<String, Object> seedCustomers() {
        log.info("开始生成测试客户数据");
        
        Faker faker = new Faker(new Locale("zh_CN"));
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String email = faker.internet().emailAddress();
            String phone = faker.phoneNumber().cellPhone();
            String companyName = faker.company().name();

            // 创建客户
            Customer customer = Customer.builder()
                .email(email)
                .hasAccount(true)
                .companyName(companyName)
                .firstName(firstName)
                .lastName(lastName)
                .metadata(Map.of(
                    "source", "seed",
                    "customerType", faker.options().option("retail", "wholesale", "vip"),
                    "registrationDate", LocalDate.now().toString(),
                    "notes", faker.lorem().sentence()
                ))
                .addresses(new ArrayList<>())
                .build();

            // 创建默认地址
            Address address = Address.builder()
                .customer(customer)
                .addressName("默认地址")
                .isDefaultShipping(true)
                .isDefaultBilling(true)
                .company(companyName)
                .firstName(firstName)
                .lastName(lastName)
                .address1(faker.address().streetAddress())
                .address2(faker.address().buildingNumber())
                .city(faker.address().city())
                .countryCode("CN")
                .province(faker.address().state())
                .postalCode(faker.address().zipCode())
                .phone(phone)
                .metadata(new HashMap<>())
                .build();

            customer = customerRepository.save(customer);
            address = addressRepository.save(address);

            customer.getAddresses().add(address);
            customer.setDefaultBillingAddressId(address.getId().toString());
            customer.setDefaultShippingAddressId(address.getId().toString());
            customer = customerRepository.save(customer);

            customers.add(customer);
            log.info("已生成测试客户: {}", customer.getEmail());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("customers", customers.stream()
            .map(this::formatCustomerResponse)
            .collect(Collectors.toList()));
        response.put("count", customers.size());

        log.info("测试客户数据生成完成");
        return response;
    }
} 