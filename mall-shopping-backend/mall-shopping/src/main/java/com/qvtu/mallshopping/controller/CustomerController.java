package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CustomerCreateRequest;
import com.qvtu.mallshopping.dto.CustomerUpdateRequest;
import com.qvtu.mallshopping.dto.CustomerGroupCreateRequest;
import com.qvtu.mallshopping.dto.CustomerGroupUpdateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@Slf4j
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCustomers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取客户列表请求, page: {}, size: {}", page, size);
        Map<String, Object> response = customerService.listCustomers(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody CustomerCreateRequest request) {
        log.info("收到创建客户请求: {}", request);
        Map<String, Object> response = customerService.createCustomer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable Long id) {
        log.info("收到获取客户详情请求, ID: {}", id);
        Map<String, Object> response = customerService.getCustomer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
        @PathVariable Long id,
        @RequestBody CustomerUpdateRequest request
    ) {
        log.info("收到更新客户请求, ID: {}, 请求数据: {}", id, request);
        Map<String, Object> response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        log.info("收到删除客户请求, ID: {}", id);
        Map<String, Object> response = customerService.deleteCustomer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedCustomers() {
        log.info("收到生成测试客户数据请求");
        Map<String, Object> response = customerService.seedCustomers();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/customer-groups")
    public ResponseEntity<Map<String, Object>> addCustomerToGroup(
        @PathVariable Long id,
        @RequestBody Map<String, List<String>> request
    ) {
        log.info("收到添加客户到群组请求, 客户ID: {}, 请求数据: {}", id, request);
        Map<String, Object> response = customerService.addCustomerToGroup(id, request.get("groupIds"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/customer-groups/remove")
    public ResponseEntity<Map<String, Object>> removeCustomerFromGroup(
        @PathVariable Long id,
        @RequestBody Map<String, List<String>> request
    ) {
        log.info("收到从群组移除客户请求, 客户ID: {}, 请求数据: {}", id, request);
        Map<String, Object> response = customerService.removeCustomerFromGroup(id, request.get("groupIds"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/seed")
    public ResponseEntity<Map<String, Object>> seedCustomerGroups() {
        log.info("收到生成测试客户群组数据请求");
        Map<String, Object> response = customerService.seedCustomerGroups();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> listCustomerGroups(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取客户群组列表请求, page: {}, size: {}", page, size);
        Map<String, Object> response = customerService.listCustomerGroups(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups")
    public ResponseEntity<Map<String, Object>> createCustomerGroup(
        @RequestBody CustomerGroupCreateRequest request
    ) {
        log.info("收到创建客户群组请求: {}", request);
        Map<String, Object> response = customerService.createCustomerGroup(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerGroup(@PathVariable Long id) {
        log.info("收到获取客户群组详情请求, ID: {}", id);
        try {
            Map<String, Object> response = customerService.getCustomerGroup(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取客户群组详情失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomerGroup(
        @PathVariable Long id,
        @RequestBody CustomerGroupUpdateRequest request
    ) {
        log.info("收到更新客户群组请求, ID: {}, 请求数据: {}", id, request);
        try {
            Map<String, Object> response = customerService.updateCustomerGroup(id, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新客户群组失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/groups/{id}/customers")
    public ResponseEntity<Map<String, Object>> addCustomersToGroup(
        @PathVariable Long id,
        @RequestBody Map<String, Object> requestBody
    ) {
        log.info("收到向客户群组添加客户请求, 群组ID: {}, 请求数据: {}", id, requestBody);
        try {
            // 检查请求体中是否包含 customer_ids
            if (!requestBody.containsKey("customer_ids")) {
                log.warn("请求中缺少 customer_ids 字段");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "必须提供要添加的客户ID列表")
                );
            }
            
            Object customerIdsObj = requestBody.get("customer_ids");
            log.info("customer_ids 类型: {}, 值: {}", 
                     customerIdsObj != null ? customerIdsObj.getClass().getName() : "null", 
                     customerIdsObj);
            
            List<String> customerIds;
            if (customerIdsObj instanceof List) {
                customerIds = (List<String>) customerIdsObj;
            } else {
                log.warn("customer_ids 不是有效的列表");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "customer_ids 必须是一个列表")
                );
            }
            
            if (customerIds.isEmpty()) {
                log.warn("customer_ids 列表为空");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "必须提供要添加的客户ID列表")
                );
            }
            
            Map<String, Object> response = customerService.addCustomersToGroup(id, customerIds);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("向客户群组添加客户失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/groups/{id}/customers")
    public ResponseEntity<Map<String, Object>> removeCustomersFromGroup(
        @PathVariable Long id,
        @RequestBody Map<String, Object> requestBody
    ) {
        log.info("收到从客户群组移除客户请求, 群组ID: {}, 请求数据: {}", id, requestBody);
        try {
            // 检查请求体中是否包含 customer_ids
            if (!requestBody.containsKey("customer_ids")) {
                log.warn("请求中缺少 customer_ids 字段");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "必须提供要移除的客户ID列表")
                );
            }
            
            Object customerIdsObj = requestBody.get("customer_ids");
            log.info("customer_ids 类型: {}, 值: {}", 
                     customerIdsObj != null ? customerIdsObj.getClass().getName() : "null", 
                     customerIdsObj);
            
            List<String> customerIds;
            if (customerIdsObj instanceof List) {
                customerIds = (List<String>) customerIdsObj;
            } else {
                log.warn("customer_ids 不是有效的列表");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "customer_ids 必须是一个列表")
                );
            }
            
            if (customerIds.isEmpty()) {
                log.warn("customer_ids 列表为空");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("message", "必须提供要移除的客户ID列表")
                );
            }
            
            customerService.removeCustomersFromGroup(id, customerIds);
            return ResponseEntity.ok(Collections.emptyMap());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("从客户群组移除客户失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomerGroup(@PathVariable Long id) {
        log.info("收到删除客户群组请求, ID: {}", id);
        try {
            Map<String, Object> response = customerService.deleteCustomerGroup(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("删除客户群组失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/groups/{id}/customers")
    public ResponseEntity<Map<String, Object>> listCustomersInGroup(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("收到获取客户群组中的客户列表请求, 群组ID: {}, page: {}, size: {}", id, page, size);
        try {
            Map<String, Object> response = customerService.listCustomersInGroup(id, page, size);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取客户群组中的客户列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 