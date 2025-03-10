package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.service.CategoryService;
import com.qvtu.mallshopping.dto.CategoryResponseDTO;
import com.qvtu.mallshopping.dto.CategoryCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long parentId
    ) {
        try {
            System.out.println("\n=== 开始获取分类列表 ===");
            System.out.println("查询参数: " + String.format(
                "name=%s, page=%d, size=%d, parentId=%s",
                name, page, size, parentId));

            List<CategoryResponseDTO> categories;
            long total;

            if (parentId != null) {
                categories = categoryService.getChildCategories(parentId, page, size);
                total = categoryService.countChildCategories(parentId);
            } else {
                categories = categoryService.listCategories(name, page, size);
                total = categoryService.countCategories(name);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            response.put("count", total);
            response.put("offset", page * size);
            response.put("limit", size);

            System.out.println("返回分类数量: " + categories.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取分类列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryCreateRequest request) {
        try {
            System.out.println("\n=== 开始创建分类 ===");
            System.out.println("请求数据: " + request);

            // 基本验证
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Category category = categoryService.createCategory(request);
            CategoryResponseDTO response = categoryService.convertToDTO(category);
            
            System.out.println("分类创建成功: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("创建分类失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


} 