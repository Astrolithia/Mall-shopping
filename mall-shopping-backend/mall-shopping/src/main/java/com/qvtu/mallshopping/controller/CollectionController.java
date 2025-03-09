package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CollectionCreateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Collection;
import com.qvtu.mallshopping.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCollections(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            System.out.println("=== 开始获取系列列表 ===");
            System.out.println("查询参数: " + String.format("title=%s, page=%d, size=%d", title, page, size));

            List<Collection> collections = collectionService.listCollections(title, page, size);
            long total = collectionService.countCollections(title);

            Map<String, Object> response = new HashMap<>();
            response.put("collections", collections);
            response.put("count", total);
            response.put("offset", page * size);
            response.put("limit", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误日志
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(@RequestBody CollectionCreateRequest request) {
        try {
            System.out.println("接收到的请求数据: " + request); // 添加日志
            Collection collection = collectionService.createCollection(request);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误日志
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Collection> getCollection(@PathVariable Long id) {
        try {
            System.out.println("=== 开始获取系列详情 ===");
            System.out.println("系列ID: " + id);
            
            Collection collection = collectionService.getCollection(id, true);
            System.out.println("查询到的系列: " + collection);
            
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            System.err.println("获取系列详情失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(
            @PathVariable Long id,
            @RequestBody CollectionCreateRequest request) {
        try {
            System.out.println("=== 开始处理更新系列请求 ===");
            System.out.println("系列ID: " + id);
            System.out.println("更新数据: " + request);

            Collection collection = collectionService.updateCollection(id, request);
            
            System.out.println("更新成功: " + collection);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(@PathVariable Long id) {
        try {
            collectionService.deleteCollection(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误日志
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/products")
    public ResponseEntity<Collection> addProductsToCollection(
            @PathVariable Long id,
            @RequestBody List<Long> productIds) {
        try {
            System.out.println("=== 开始添加产品到系列 ===");
            System.out.println("系列ID: " + id);
            System.out.println("接收到的产品IDs: " + productIds);

            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Collection collection = collectionService.addProductsToCollection(id, productIds);
            
            System.out.println("添加成功: " + collection);
            return ResponseEntity.ok(collection);
        } catch (ResourceNotFoundException e) {
            System.err.println("资源不存在: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("添加产品到系列失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/products")
    public ResponseEntity<Collection> removeProductsFromCollection(
            @PathVariable Long id,
            @RequestBody List<Long> productIds) {
        try {
            System.out.println("=== 开始从系列移除产品 ===");
            System.out.println("系列ID: " + id);
            System.out.println("产品IDs: " + productIds);

            Collection collection = collectionService.removeProductsFromCollection(id, productIds);
            
            System.out.println("移除成功: " + collection);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            System.err.println("从系列移除产品失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<Map<String, Object>> getCollectionProducts(@PathVariable Long id) {
        try {
            System.out.println("\n=== 开始获取系列中的产品 ===");
            System.out.println("系列ID: " + id);

            Collection collection = collectionService.getCollection(id, true);
            System.out.println("找到系列: " + collection);

            List<Map<String, Object>> products = collection.getProductDTOs();
            if (products == null) {
                products = new ArrayList<>();
            }
            System.out.println("系列中的产品: " + products);
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("count", products.size());
            response.put("offset", 0);
            response.put("limit", 50);
            
            System.out.println("返回的响应数据: " + response);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            System.err.println("系列不存在: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("获取系列产品失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
