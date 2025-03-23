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
import com.qvtu.mallshopping.model.CustomerGroup;
import com.qvtu.mallshopping.repository.CustomerGroupRepository;
import com.qvtu.mallshopping.dto.CustomerGroupCreateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.dto.CustomerGroupUpdateRequest;
import java.util.Collections;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.qvtu.mallshopping.dto.CustomerRegisterRequest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.NonUniqueResultException;

@Service
@Slf4j
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        formatted.put("is_default_shipping", address.isDefaultShipping());
        formatted.put("is_default_billing", address.isDefaultBilling());
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
                customer.setDefaultBillingAddress(address);
                customer.setDefaultShippingAddress(address);
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
                .filter(addr -> Boolean.TRUE.equals(addr.isDefaultBilling()))
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
                customer.setDefaultBillingAddress(defaultAddress);
                customer.setDefaultShippingAddress(defaultAddress);
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
            customer.setDefaultBillingAddress(address);
            customer.setDefaultShippingAddress(address);
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

    public Map<String, Object> createTestCustomerGroup() {
        log.info("开始创建测试客户群组");
        
        try {
            Faker faker = new Faker(new Locale("zh_CN"));
            
            // 生成随机群组名称
            String groupName = faker.commerce().department() + "客户组-" + 
                              faker.number().randomNumber(4, true);
            
            // 创建一个测试群组
            CustomerGroup group = CustomerGroup.builder()
                .name(groupName)
                .metadata(Map.of(
                    "description", faker.commerce().productName() + " - " + faker.company().catchPhrase(),
                    "discount", faker.options().option("5%", "10%", "15%", "20%", "25%"),
                    "createdAt", LocalDate.now().toString(),
                    "type", faker.options().option("retail", "wholesale", "vip"),
                    "level", faker.options().option("bronze", "silver", "gold", "platinum"),
                    "tags", faker.commerce().department() + "," + faker.commerce().material()
                ))
                .customers(new ArrayList<>())
                .build();

            group = customerGroupRepository.save(group);
            log.info("测试客户群组创建成功, ID: {}, 名称: {}", group.getId(), group.getName());

            // 格式化响应
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("id", group.getId().toString());
            groupData.put("name", group.getName());
            groupData.put("metadata", group.getMetadata());

            Map<String, Object> response = new HashMap<>();
            response.put("customer_group", groupData);
            
            return response;
        } catch (Exception e) {
            log.error("创建测试客户群组失败", e);
            throw e;
        }
    }

    public Map<String, Object> addCustomerToGroup(Long customerId, List<String> groupIds) {
        log.info("开始将客户添加到群组, 客户ID: {}, 群组IDs: {}", customerId, groupIds);
        
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

            // 添加客户到指定的群组
            if (groupIds != null && !groupIds.isEmpty()) {
                for (String groupId : groupIds) {
                    CustomerGroup group = customerGroupRepository.findById(Long.parseLong(groupId))
                        .orElseThrow(() -> new RuntimeException("Customer group not found: " + groupId));

                    if (!customer.getCustomerGroups().contains(group)) {
                        customer.getCustomerGroups().add(group);
                        log.info("客户已添加到群组: {}", group.getName());
                    }
                }
            }
            
            customer = customerRepository.save(customer);
            log.info("客户群组更新成功");

            Map<String, Object> response = new HashMap<>();
            response.put("customer", formatCustomerResponse(customer));
            
            return response;
        } catch (Exception e) {
            log.error("添加客户到群组失败", e);
            throw e;
        }
    }

    public Map<String, Object> removeCustomerFromGroup(Long customerId, List<String> groupIds) {
        log.info("开始将客户从群组移除, 客户ID: {}, 群组IDs: {}", customerId, groupIds);
        
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

            // 从指定的群组中移除客户
            if (groupIds != null && !groupIds.isEmpty()) {
                for (String groupId : groupIds) {
                    CustomerGroup group = customerGroupRepository.findById(Long.parseLong(groupId))
                        .orElseThrow(() -> new RuntimeException("Customer group not found: " + groupId));

                    customer.getCustomerGroups().remove(group);
                    log.info("客户已从群组移除: {}", group.getName());
                }
            }
            
            customer = customerRepository.save(customer);
            log.info("客户群组更新成功");

            Map<String, Object> response = new HashMap<>();
            response.put("customer", formatCustomerResponse(customer));
            
            return response;
        } catch (Exception e) {
            log.error("从群组移除客户失败", e);
            throw e;
        }
    }

    public Map<String, Object> listCustomerGroups(int page, int size) {
        log.info("获取客户群组列表, 页码: {}, 每页数量: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerGroup> groupPage = customerGroupRepository.findAll(pageable);
            
            List<Map<String, Object>> formattedGroups = groupPage.getContent().stream()
                .map(this::formatCustomerGroupResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("customer_groups", formattedGroups);
            response.put("count", groupPage.getTotalElements());
            response.put("limit", size);
            response.put("offset", page * size);

            return response;
        } catch (Exception e) {
            log.error("获取客户群组列表失败", e);
            throw e;
        }
    }

    private Map<String, Object> formatCustomerGroupResponse(CustomerGroup group) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", group.getId().toString());
        formatted.put("name", group.getName());
        formatted.put("metadata", group.getMetadata());
        formatted.put("created_at", group.getCreatedAt());
        formatted.put("updated_at", group.getUpdatedAt());
        
        // 格式化客户列表
        List<Map<String, Object>> customers = group.getCustomers().stream()
            .map(this::formatCustomerResponse)
            .collect(Collectors.toList());
        formatted.put("customers", customers);
        
        return formatted;
    }

    public Map<String, Object> createCustomerGroup(CustomerGroupCreateRequest request) {
        log.info("开始创建客户群组, 请求数据: {}", request);
        
        try {
            // 检查名称是否已存在
            if (customerGroupRepository.existsByName(request.getName())) {
                throw new RuntimeException("Customer group with this name already exists");
            }

            // 创建新客户群组
            CustomerGroup group = CustomerGroup.builder()
                .name(request.getName())
                .metadata(request.getMetadata())
                .customers(new ArrayList<>())
                .build();

            group = customerGroupRepository.save(group);
            log.info("客户群组创建成功, ID: {}", group.getId());

            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("customer_group", formatCustomerGroupResponse(group));
            
            return response;
        } catch (Exception e) {
            log.error("创建客户群组失败", e);
            throw e;
        }
    }

    public Map<String, Object> getCustomerGroup(Long id) {
        log.info("获取客户群组详情, ID: {}", id);
        
        try {
            CustomerGroup group = customerGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("customer_group", formatCustomerGroupResponse(group));
            
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("获取客户群组详情失败, ID: {}", id, e);
            throw e;
        }
    }

    public Map<String, Object> updateCustomerGroup(Long id, CustomerGroupUpdateRequest request) {
        log.info("开始更新客户群组, ID: {}, 请求数据: {}", id, request);
        
        try {
            // 查找客户群组
            CustomerGroup group = customerGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            // 检查名称是否已存在（如果更新了名称）
            if (request.getName() != null && !request.getName().equals(group.getName())) {
                if (customerGroupRepository.existsByName(request.getName())) {
                    throw new RuntimeException("Customer group with this name already exists");
                }
                group.setName(request.getName());
            }
            
            // 更新元数据
            if (request.getMetadata() != null) {
                group.setMetadata(request.getMetadata());
            }
            
            // 保存更新
            group = customerGroupRepository.save(group);
            log.info("客户群组更新成功, ID: {}", group.getId());
            
            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("customer_group", formatCustomerGroupResponse(group));
            
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("更新客户群组失败, ID: {}", id, e);
            throw e;
        }
    }

    public Map<String, Object> seedCustomerGroups() {
        log.info("开始生成测试客户群组数据");
        
        Faker faker = new Faker(new Locale("zh_CN"));
        List<CustomerGroup> groups = new ArrayList<>();
        
        // 获取所有客户，用于随机分配到群组
        List<Customer> allCustomers = customerRepository.findAll();
        if (allCustomers.isEmpty()) {
            log.warn("没有客户数据，先生成一些客户数据");
            seedCustomers();
            allCustomers = customerRepository.findAll();
        }
        
        // 定义一些常见的客户群组类型
        String[] groupTypes = {
            "VIP客户", "新客户", "高频消费", "批发客户", "零售客户", 
            "企业客户", "个人客户", "会员", "潜在客户", "流失客户"
        };
        
        // 创建5个客户群组
        for (int i = 0; i < 5; i++) {
            String groupName = groupTypes[i % groupTypes.length];
            if (i > 0) {
                groupName += "-" + faker.commerce().department();
            }
            
            // 检查名称是否已存在
            if (customerGroupRepository.existsByName(groupName)) {
                groupName += "-" + faker.number().digits(3);
            }
            
            // 创建群组元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("description", faker.lorem().sentence());
            metadata.put("discount", faker.options().option("5%", "10%", "15%", "20%", "25%"));
            metadata.put("priority", faker.number().numberBetween(1, 10));
            metadata.put("tags", List.of(
                faker.commerce().material(),
                faker.commerce().department(),
                faker.commerce().color()
            ));
            
            // 创建客户群组
            CustomerGroup group = CustomerGroup.builder()
                .name(groupName)
                .metadata(metadata)
                .customers(new ArrayList<>())
                .build();
            
            group = customerGroupRepository.save(group);
            
            // 随机选择1-3个客户添加到群组
            int customerCount = faker.number().numberBetween(1, Math.min(4, allCustomers.size() + 1));
            for (int j = 0; j < customerCount; j++) {
                // 随机选择一个客户
                Customer customer = allCustomers.get(faker.number().numberBetween(0, allCustomers.size()));
                
                // 添加客户到群组
                if (!group.getCustomers().contains(customer)) {
                    group.getCustomers().add(customer);
                    
                    // 更新客户的群组列表
                    if (customer.getCustomerGroups() == null) {
                        customer.setCustomerGroups(new ArrayList<>());
                    }
                    customer.getCustomerGroups().add(group);
                    customerRepository.save(customer);
                }
            }
            
            // 保存更新后的群组
            group = customerGroupRepository.save(group);
            groups.add(group);
            
            log.info("已生成测试客户群组: {}, 包含 {} 个客户", group.getName(), group.getCustomers().size());
        }
        
        // 格式化响应
        Map<String, Object> response = new HashMap<>();
        response.put("customer_groups", groups.stream()
            .map(this::formatCustomerGroupResponse)
            .collect(Collectors.toList()));
        response.put("count", groups.size());
        
        log.info("测试客户群组数据生成完成");
        return response;
    }

    public Map<String, Object> addCustomersToGroup(Long groupId, List<String> customerIds) {
        log.info("开始向客户群组添加客户, 群组ID: {}, 客户IDs: {}", groupId, customerIds);
        
        try {
            // 查找客户群组
            CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            List<Customer> addedCustomers = new ArrayList<>();
            
            // 添加客户到群组
            for (String customerId : customerIds) {
                try {
                    Long id = Long.parseLong(customerId);
                    Customer customer = customerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
                    
                    // 检查客户是否已在群组中
                    if (!group.getCustomers().contains(customer)) {
                        group.getCustomers().add(customer);
                        
                        // 更新客户的群组列表
                        if (customer.getCustomerGroups() == null) {
                            customer.setCustomerGroups(new ArrayList<>());
                        }
                        if (!customer.getCustomerGroups().contains(group)) {
                            customer.getCustomerGroups().add(group);
                            customerRepository.save(customer);
                        }
                        
                        addedCustomers.add(customer);
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的客户ID格式: {}", customerId);
                }
            }
            
            // 保存更新后的群组
            group = customerGroupRepository.save(group);
            log.info("成功向客户群组添加 {} 个客户", addedCustomers.size());
            
            // 格式化响应
            Map<String, Object> response = new HashMap<>();
            response.put("customer_group", formatCustomerGroupResponse(group));
            
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", groupId);
            throw e;
        } catch (Exception e) {
            log.error("向客户群组添加客户失败, 群组ID: {}", groupId, e);
            throw e;
        }
    }

    public void removeCustomersFromGroup(Long groupId, List<String> customerIds) {
        log.info("开始从客户群组移除客户, 群组ID: {}, 客户IDs: {}", groupId, customerIds);
        
        try {
            // 查找客户群组
            CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            int removedCount = 0;
            
            // 从群组中移除客户
            for (String customerId : customerIds) {
                try {
                    Long id = Long.parseLong(customerId);
                    Customer customer = customerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
                    
                    // 从群组中移除客户
                    if (group.getCustomers().remove(customer)) {
                        removedCount++;
                        
                        // 从客户的群组列表中移除此群组
                        if (customer.getCustomerGroups() != null) {
                            customer.getCustomerGroups().remove(group);
                            customerRepository.save(customer);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的客户ID格式: {}", customerId);
                }
            }
            
            // 保存更新后的群组
            customerGroupRepository.save(group);
            log.info("成功从客户群组移除 {} 个客户", removedCount);
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", groupId);
            throw e;
        } catch (Exception e) {
            log.error("从客户群组移除客户失败, 群组ID: {}", groupId, e);
            throw e;
        }
    }

    public Map<String, Object> deleteCustomerGroup(Long id) {
        log.info("开始删除客户群组, ID: {}", id);
        
        try {
            // 查找客户群组
            CustomerGroup group = customerGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            // 从所有关联的客户中移除此群组
            for (Customer customer : group.getCustomers()) {
                if (customer.getCustomerGroups() != null) {
                    customer.getCustomerGroups().remove(group);
                    customerRepository.save(customer);
                }
            }
            
            // 删除客户群组
            customerGroupRepository.delete(group);
            log.info("客户群组删除成功, ID: {}", id);
            
            // 返回符合 Medusa API 规范的响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", id.toString());
            response.put("object", "customer_group");
            response.put("deleted", true);
            
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("删除客户群组失败, ID: {}", id, e);
            throw e;
        }
    }

    public Map<String, Object> listCustomersInGroup(Long groupId, int page, int size) {
        log.info("获取客户群组中的客户列表, 群组ID: {}, 页码: {}, 每页数量: {}", groupId, page, size);
        
        try {
            // 查找客户群组
            CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer group not found"));
            
            // 获取群组中的客户
            List<Customer> customers = group.getCustomers();
            
            // 分页处理
            int start = page * size;
            int end = Math.min(start + size, customers.size());
            
            List<Customer> pagedCustomers;
            if (start < customers.size()) {
                pagedCustomers = customers.subList(start, end);
            } else {
                pagedCustomers = new ArrayList<>();
            }
            
            // 格式化客户列表
            List<Map<String, Object>> formattedCustomers = pagedCustomers.stream()
                .map(this::formatCustomerResponse)
                .collect(Collectors.toList());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("customers", formattedCustomers);
            response.put("count", customers.size());
            response.put("offset", page * size);
            response.put("limit", size);
            
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("客户群组不存在, ID: {}", groupId);
            throw e;
        } catch (Exception e) {
            log.error("获取客户群组中的客户列表失败, 群组ID: {}", groupId, e);
            throw e;
        }
    }

    @Transactional
    public Customer registerCustomer(CustomerRegisterRequest request) {
        // 检查邮箱是否已被注册
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        
        // 创建新客户
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setHasAccount(true);
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        
        // 保存客户并返回
        return customerRepository.save(customer);
    }
    
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    /**
     * 验证客户凭据并返回客户信息
     */
    public Customer authenticateCustomer(String email, String password) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // 验证密码
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        return customer;
    }

    /**
     * 创建基本客户（只有邮箱和密码）
     */
    @Transactional
    public Customer createBasicCustomer(String email, String password) {
        // 检查邮箱是否已被注册
        if (customerRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        
        // 创建新客户
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setHasAccount(true);
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        
        // 保存客户并返回
        return customerRepository.save(customer);
    }

    /**
     * 使用重置令牌重置密码
     */
    public Customer resetPassword(String resetToken, String newPassword) {
        try {
            if (resetToken == null || resetToken.isEmpty()) {
                throw new RuntimeException("Reset token cannot be empty");
            }
            
            // 使用新的查询方法
            Optional<Customer> customerOpt = customerRepository.findByResetPasswordTokenEquals(resetToken);
            
            if (!customerOpt.isPresent()) {
                throw new RuntimeException("Invalid reset token");
            }
            
            Customer customer = customerOpt.get();
            
            // 验证令牌是否过期
            if (customer.getResetPasswordTokenExpiresAt() == null || 
                customer.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired");
            }
            
            // 更新密码
            customer.setPassword(passwordEncoder.encode(newPassword));
            
            // 清除重置令牌
            customer.setResetPasswordToken(null);
            customer.setResetPasswordTokenExpiresAt(null);
            
            // 保存更新后的用户
            return customerRepository.save(customer);
        } catch (NonUniqueResultException e) {
            // 这种情况现在应该不会发生了，但保留作为安全措施
            log.error("发现重复的密码重置令牌: {}", resetToken);
            throw new RuntimeException("System error: duplicate reset tokens found");
        }
    }

    /**
     * 请求密码重置
     */
    public void requestPasswordReset(String email) {
        // 查找用户，但不透露用户是否存在
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            
            // 生成唯一的重置令牌
            String resetToken;
            boolean isUnique;
            
            do {
                resetToken = UUID.randomUUID().toString();
                // 使用新的查询方法检查令牌是否已存在
                isUnique = !customerRepository.findByResetPasswordTokenEquals(resetToken).isPresent();
            } while (!isUnique);
            
            // 设置令牌有效期（例如24小时）
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            
            // 存储令牌和过期时间
            customer.setResetPasswordToken(resetToken);
            customer.setResetPasswordTokenExpiresAt(expiresAt);
            customerRepository.save(customer);
            
            // 打印令牌用于测试
            log.info("重置密码令牌已生成: {} 用于邮箱: {}", resetToken, email);
        }
    }

    /**
     * 更新用户密码
     * 
     * @param userId 用户ID，从JWT令牌中获取
     * @param email 用户邮箱
     * @param newPassword 新密码
     */
    @Transactional
    public void updatePassword(Long userId, String email, String newPassword) {
        // 通过ID获取用户
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 验证邮箱是否匹配
        if (!customer.getEmail().equals(email)) {
            throw new RuntimeException("Email does not match the authenticated user");
        }
        
        // 更新密码
        customer.setPassword(passwordEncoder.encode(newPassword));
        
        // 保存更新
        customerRepository.save(customer);
    }

    /**
     * 创建商店客户
     * 
     * @param email 电子邮箱（必填）
     * @param companyName 公司名称（可选）
     * @param firstName 名字（可选）
     * @param lastName 姓氏（可选）
     * @param phone 电话（可选）
     * @param metadata 元数据（可选）
     * @return 创建的客户对象
     */
    @Transactional
    public Customer createStoreCustomer(String email, String companyName, String firstName, 
                                  String lastName, String phone, Map<String, Object> metadata) {
        // 检查邮箱是否为空
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        // 检查邮箱是否已被注册
        if (customerRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        
        // 创建新客户
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setCompanyName(companyName);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhone(phone);
        customer.setHasAccount(true);
        
        // 设置元数据
        if (metadata != null) {
            customer.setMetadata(metadata);
        } else {
            customer.setMetadata(new HashMap<>());
        }
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        
        // 保存并返回客户
        return customerRepository.save(customer);
    }

    /**
     * 更新客户信息
     * 
     * @param customerId 客户ID
     * @param companyName 公司名称（可选）
     * @param firstName 名字（可选）
     * @param lastName 姓氏（可选）
     * @param phone 电话（可选）
     * @param metadata 元数据（可选）
     * @return 更新后的客户对象
     */
    @Transactional
    public Customer updateCustomerInfo(Long customerId, String companyName, String firstName, 
                             String lastName, String phone, Map<String, Object> metadata) {
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 更新客户信息（仅更新非null字段）
        if (companyName != null) {
            customer.setCompanyName(companyName);
        }
        if (firstName != null) {
            customer.setFirstName(firstName);
        }
        if (lastName != null) {
            customer.setLastName(lastName);
        }
        if (phone != null) {
            customer.setPhone(phone);
        }
        if (metadata != null) {
            // 合并现有元数据和新元数据
            Map<String, Object> existingMetadata = customer.getMetadata();
            if (existingMetadata == null) {
                existingMetadata = new HashMap<>();
            }
            existingMetadata.putAll(metadata);
            customer.setMetadata(existingMetadata);
        }
        
        // 更新时间戳
        customer.setUpdatedAt(LocalDateTime.now());
        
        // 保存并返回更新后的客户
        return customerRepository.save(customer);
    }

    /**
     * 获取客户地址列表
     * 
     * @param customerId 客户ID
     * @param limit 每页限制
     * @param offset 偏移量
     * @return 地址列表及分页信息
     */
    public Map<String, Object> getCustomerAddresses(Long customerId, int limit, int offset) {
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 获取所有地址
        List<Address> addresses = customer.getAddresses();
        
        // 计算总数
        int count = addresses.size();
        
        // 应用分页
        List<Address> pagedAddresses;
        if (offset < count) {
            int endIndex = Math.min(offset + limit, count);
            pagedAddresses = addresses.subList(offset, endIndex);
        } else {
            pagedAddresses = new ArrayList<>();
        }
        
        // 格式化响应
        List<Map<String, Object>> formattedAddresses = pagedAddresses.stream()
                .map(this::formatAddressResponse)
                .collect(Collectors.toList());
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("addresses", formattedAddresses);
        response.put("count", count);
        response.put("limit", limit);
        response.put("offset", offset);
        
        return response;
    }

    /**
     * 为客户添加新地址
     * 
     * @param customerId 客户ID
     * @param firstName 名字
     * @param lastName 姓氏
     * @param phone 电话号码
     * @param company 公司名称
     * @param address1 地址行1
     * @param address2 地址行2
     * @param city 城市
     * @param countryCode 国家代码
     * @param province 省/州
     * @param postalCode 邮政编码
     * @param addressName 地址名称
     * @param metadata 元数据
     * @return 更新后的客户对象
     */
    @Transactional
    public Customer addCustomerAddress(
            Long customerId, String firstName, String lastName, String phone,
            String company, String address1, String address2, String city,
            String countryCode, String province, String postalCode,
            String addressName, Map<String, Object> metadata) {
        
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 创建新地址
        Address address = new Address();
        address.setCustomer(customer);
        address.setFirstName(firstName);
        address.setLastName(lastName);
        address.setPhone(phone);
        address.setCompany(company);
        address.setAddress1(address1);
        address.setAddress2(address2);
        address.setCity(city);
        address.setCountryCode(countryCode);
        address.setProvince(province);
        address.setPostalCode(postalCode);
        address.setAddressName(addressName);
        
        // 设置元数据
        if (metadata != null) {
            address.setMetadata(metadata);
        }
        
        // 如果是客户的第一个地址，则设置为默认地址
        if (customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
            address.setDefaultShipping(true);
            address.setDefaultBilling(true);
            customer.setDefaultShippingAddress(address);
            customer.setDefaultBillingAddress(address);
        }
        
        // 保存地址
        Address savedAddress = addressRepository.save(address);
        
        // 更新客户地址列表
        if (customer.getAddresses() == null) {
            customer.setAddresses(new ArrayList<>());
        }
        customer.getAddresses().add(savedAddress);
        
        // 如果设置了默认地址，更新客户的默认地址引用
        if (address.isDefaultShipping()) {
            customer.setDefaultShippingAddress(savedAddress);
        }
        if (address.isDefaultBilling()) {
            customer.setDefaultBillingAddress(savedAddress);
        }
        
        // 保存并返回更新后的客户
        return customerRepository.save(customer);
    }

    /**
     * 获取客户的特定地址
     * 
     * @param customerId 客户ID
     * @param addressId 地址ID
     * @return 地址对象
     * @throws RuntimeException 如果地址不存在或不属于该客户
     */
    public Address getCustomerAddress(Long customerId, Long addressId) {
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 获取地址
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // 验证地址是否属于该客户
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to the customer");
        }
        
        return address;
    }

    /**
     * 更新客户地址
     * 
     * @param customerId 客户ID
     * @param addressId 要更新的地址ID
     * @param firstName 名字（可选）
     * @param lastName 姓氏（可选）
     * @param phone 电话号码（可选）
     * @param company 公司名称（可选）
     * @param address1 地址行1（可选）
     * @param address2 地址行2（可选）
     * @param city 城市（可选）
     * @param countryCode 国家代码（可选）
     * @param province 省/州（可选）
     * @param postalCode 邮政编码（可选）
     * @param addressName 地址名称（可选）
     * @param metadata 元数据（可选）
     * @param isDefaultShipping 是否为默认配送地址（可选）
     * @param isDefaultBilling 是否为默认账单地址（可选）
     * @return 更新后的客户对象
     */
    @Transactional
    public Customer updateCustomerAddress(
            Long customerId, Long addressId, String firstName, String lastName,
            String phone, String company, String address1, String address2,
            String city, String countryCode, String province, String postalCode,
            String addressName, Map<String, Object> metadata,
            Boolean isDefaultShipping, Boolean isDefaultBilling) {
        
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 获取地址
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // 验证地址是否属于该客户
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to the customer");
        }
        
        // 更新地址信息（仅更新非null字段）
        if (firstName != null) {
            address.setFirstName(firstName);
        }
        if (lastName != null) {
            address.setLastName(lastName);
        }
        if (phone != null) {
            address.setPhone(phone);
        }
        if (company != null) {
            address.setCompany(company);
        }
        if (address1 != null) {
            address.setAddress1(address1);
        }
        if (address2 != null) {
            address.setAddress2(address2);
        }
        if (city != null) {
            address.setCity(city);
        }
        if (countryCode != null) {
            address.setCountryCode(countryCode);
        }
        if (province != null) {
            address.setProvince(province);
        }
        if (postalCode != null) {
            address.setPostalCode(postalCode);
        }
        if (addressName != null) {
            address.setAddressName(addressName);
        }
        if (metadata != null) {
            // 合并现有元数据和新元数据
            Map<String, Object> existingMetadata = address.getMetadata();
            if (existingMetadata == null) {
                existingMetadata = new HashMap<>();
            }
            existingMetadata.putAll(metadata);
            address.setMetadata(existingMetadata);
        }
        
        // 保存更新后的地址
        addressRepository.save(address);
        
        // 处理默认地址设置
        if (isDefaultShipping != null) {
            address.setDefaultShipping(isDefaultShipping);
            if (isDefaultShipping) {
                customer.setDefaultShippingAddress(address);
            } else if (customer.getDefaultShippingAddress() != null && 
                       customer.getDefaultShippingAddress().getId().equals(address.getId())) {
                customer.setDefaultShippingAddress(null);
            }
        }

        if (isDefaultBilling != null) {
            address.setDefaultBilling(isDefaultBilling);
            if (isDefaultBilling) {
                customer.setDefaultBillingAddress(address);
            } else if (customer.getDefaultBillingAddress() != null && 
                       customer.getDefaultBillingAddress().getId().equals(address.getId())) {
                customer.setDefaultBillingAddress(null);
            }
        }
        
        // 返回更新后的客户（包含所有地址）
        return customer;
    }

    /**
     * 删除客户地址
     * 
     * @param customerId 客户ID
     * @param addressId 要删除的地址ID
     * @return 被删除的地址ID
     * @throws RuntimeException 如果地址不存在或不属于该客户
     */
    @Transactional
    public Long deleteCustomerAddress(Long customerId, Long addressId) {
        // 获取客户
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        // 获取地址
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        // 验证地址是否属于该客户
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("地址不属于该客户");
        }
        
        // 如果是默认地址，清除客户的默认地址引用
        if (customer.getDefaultShippingAddress() != null && 
            customer.getDefaultShippingAddress().getId().equals(addressId)) {
            customer.setDefaultShippingAddress(null);
        }
        
        if (customer.getDefaultBillingAddress() != null && 
            customer.getDefaultBillingAddress().getId().equals(addressId)) {
            customer.setDefaultBillingAddress(null);
        }
        
        // 更新客户（移除关联的地址）
        if (customer.getAddresses() != null) {
            customer.getAddresses().removeIf(a -> a.getId().equals(addressId));
            customerRepository.save(customer);
        }
        
        // 删除地址
        addressRepository.deleteById(addressId);
        
        return addressId;
    }

    /**
     * 生成随机测试地址
     * 
     * @param customerId 客户ID，地址将关联到该客户
     * @param count 要生成的地址数量
     * @return 生成的地址列表
     */
    @Transactional
    public List<Address> generateRandomAddresses(Long customerId, int count) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        Faker faker = new Faker(new Locale("zh", "CN"));
        List<Address> addresses = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Address address = Address.builder()
                    .customer(customer)
                    .addressName("地址 " + (i + 1))
                    .company(faker.company().name())
                    .firstName(faker.name().firstName())
                    .lastName(faker.name().lastName())
                    .address1(faker.address().streetAddress())
                    .address2(faker.address().secondaryAddress())
                    .city(faker.address().city())
                    .countryCode("CN")
                    .province(faker.address().state())
                    .postalCode(faker.address().zipCode())
                    .phone(faker.phoneNumber().cellPhone())
                    .isDefaultShipping(i == 0) // 第一个地址设为默认送货地址
                    .isDefaultBilling(i == 0)  // 第一个地址设为默认账单地址
                    .metadata(new HashMap<>())
                    .build();
            
            addresses.add(addressRepository.save(address));
            
            // 如果是第一个地址，设置为客户的默认地址
            if (i == 0) {
                customer.setDefaultShippingAddress(address);
                customer.setDefaultBillingAddress(address);
            }
        }
        
        // 更新客户的地址列表
        if (customer.getAddresses() == null) {
            customer.setAddresses(new ArrayList<>());
        }
        customer.getAddresses().addAll(addresses);
        customerRepository.save(customer);
        
        return addresses;
    }

} 