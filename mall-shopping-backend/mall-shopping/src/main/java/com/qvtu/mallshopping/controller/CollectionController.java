package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CollectionCreateRequest;
import com.qvtu.mallshopping.model.Collection;
import com.qvtu.mallshopping.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            
            Collection collection = collectionService.getCollection(id);
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
}
