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

            // 确保 size 不小于请求的值
            size = Math.max(size, 20);

            List<Collection> collections = collectionService.listCollections(title, page, size);
            long total = collectionService.countCollections(title);

            Map<String, Object> response = new HashMap<>();
            response.put("collections", collections);
            response.put("count", total);
            response.put("offset", page * size);
            response.put("limit", size);

            System.out.println("查询到的系列数量: " + collections.size());
            System.out.println("总记录数: " + total);
            System.out.println("返回数据: " + response);

            return ResponseEntity
                .ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(response);
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
            Collection collection = collectionService.getCollection(id);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误日志
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(
            @PathVariable Long id,
            @RequestBody CollectionCreateRequest request) {
        try {
            Collection collection = collectionService.updateCollection(id, request);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误日志
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
