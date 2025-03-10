package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.service.CategoryService;
import com.qvtu.mallshopping.dto.CategoryResponseDTO;
import com.qvtu.mallshopping.dto.CategoryCreateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategory(id);
            
            // 转换为前端期望的格式
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("id", category.getId());
            formatted.put("name", category.getName());
            formatted.put("handle", category.getHandle());
            formatted.put("description", category.getDescription());
            formatted.put("is_internal", category.getIsInternal());
            formatted.put("is_active", category.getIsActive());
            formatted.put("rank", category.getRank());
            formatted.put("parent_category_id", category.getParentCategoryId());
            formatted.put("created_at", category.getCreatedAt());
            formatted.put("updated_at", category.getUpdatedAt());
            formatted.put("deleted_at", null);
            
            // 处理父分类
            if (category.getParentCategory() != null) {
                Map<String, Object> parentCategory = new HashMap<>();
                parentCategory.put("id", category.getParentCategory().getId());
                parentCategory.put("name", category.getParentCategory().getName());
                parentCategory.put("handle", category.getParentCategory().getHandle());
                parentCategory.put("description", category.getParentCategory().getDescription());
                parentCategory.put("is_internal", category.getParentCategory().getIsInternal());
                parentCategory.put("is_active", category.getParentCategory().getIsActive());
                parentCategory.put("rank", category.getParentCategory().getRank());
                parentCategory.put("created_at", category.getParentCategory().getCreatedAt());
                parentCategory.put("updated_at", category.getParentCategory().getUpdatedAt());
                parentCategory.put("deleted_at", null);
                formatted.put("parent_category", parentCategory);
            } else {
                formatted.put("parent_category", null);
            }
            
            // 处理子分类
            formatted.put("category_children", category.getChildren() == null ? 
                new ArrayList<>() : 
                category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("id", child.getId());
                        childMap.put("name", child.getName());
                        childMap.put("handle", child.getHandle());
                        childMap.put("description", child.getDescription());
                        childMap.put("is_internal", child.getIsInternal());
                        childMap.put("is_active", child.getIsActive());
                        childMap.put("rank", child.getRank());
                        childMap.put("created_at", child.getCreatedAt());
                        childMap.put("updated_at", child.getUpdatedAt());
                        childMap.put("deleted_at", null);
                        return childMap;
                    })
                    .collect(Collectors.toList()));

            // 构造最终响应
            Map<String, Object> response = new HashMap<>();
            response.put("product_category", formatted);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("获取分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<Category> categoryPage;
            if (parentId != null) {
                categoryPage = categoryService.getCategoriesByParent(parentId, PageRequest.of(page, size));
            } else if (name != null && !name.trim().isEmpty()) {
                categoryPage = categoryService.searchCategories(name, PageRequest.of(page, size));
            } else {
                categoryPage = categoryService.getTopLevelCategories(PageRequest.of(page, size));
            }

            // 转换为前端期望的格式
            List<Map<String, Object>> formattedCategories = categoryPage.getContent().stream()
                .map(category -> {
                    Map<String, Object> formatted = new HashMap<>();
                    formatted.put("id", category.getId());
                    formatted.put("name", category.getName());
                    formatted.put("handle", category.getHandle());
                    formatted.put("description", category.getDescription());
                    formatted.put("is_internal", category.getIsInternal());
                    formatted.put("is_active", category.getIsActive());
                    formatted.put("rank", category.getRank());
                    formatted.put("parent_category_id", category.getParentCategoryId());
                    formatted.put("created_at", category.getCreatedAt());
                    formatted.put("updated_at", category.getUpdatedAt());
                    formatted.put("deleted_at", null);
                    
                    // 处理父分类
                    if (category.getParentCategory() != null) {
                        Map<String, Object> parentCategory = new HashMap<>();
                        parentCategory.put("id", category.getParentCategory().getId());
                        parentCategory.put("name", category.getParentCategory().getName());
                        // ... 其他父分类字段
                        formatted.put("parent_category", parentCategory);
                    } else {
                        formatted.put("parent_category", null);
                    }
                    
                    // 处理子分类
                    formatted.put("category_children", category.getChildren() == null ? 
                        new ArrayList<>() : 
                        category.getChildren().stream()
                            .map(child -> {
                                Map<String, Object> childMap = new HashMap<>();
                                childMap.put("id", child.getId());
                                childMap.put("name", child.getName());
                                // ... 其他子分类字段
                                return childMap;
                            })
                            .collect(Collectors.toList()));
                    
                    return formatted;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("product_categories", formattedCategories);
            response.put("count", categoryPage.getTotalElements());
            response.put("offset", page * size);
            response.put("limit", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取分类列表失败: " + e.getMessage());
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

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            
            // 转换为前端期望的格式
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("id", category.getId());
            formatted.put("name", category.getName());
            formatted.put("handle", category.getHandle());
            formatted.put("description", category.getDescription());
            formatted.put("is_internal", category.getIsInternal());
            formatted.put("is_active", category.getIsActive());
            formatted.put("rank", category.getRank());
            formatted.put("parent_category_id", category.getParentCategoryId());
            formatted.put("created_at", category.getCreatedAt());
            formatted.put("updated_at", category.getUpdatedAt());
            formatted.put("deleted_at", null);
            
            // 处理父分类
            if (category.getParentCategory() != null) {
                Map<String, Object> parentCategory = new HashMap<>();
                parentCategory.put("id", category.getParentCategory().getId());
                parentCategory.put("name", category.getParentCategory().getName());
                parentCategory.put("handle", category.getParentCategory().getHandle());
                parentCategory.put("description", category.getParentCategory().getDescription());
                parentCategory.put("is_internal", category.getParentCategory().getIsInternal());
                parentCategory.put("is_active", category.getParentCategory().getIsActive());
                parentCategory.put("rank", category.getParentCategory().getRank());
                parentCategory.put("created_at", category.getParentCategory().getCreatedAt());
                parentCategory.put("updated_at", category.getParentCategory().getUpdatedAt());
                parentCategory.put("deleted_at", null);
                formatted.put("parent_category", parentCategory);
            } else {
                formatted.put("parent_category", null);
            }
            
            // 处理子分类
            formatted.put("category_children", category.getChildren() == null ? 
                new ArrayList<>() : 
                category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("id", child.getId());
                        childMap.put("name", child.getName());
                        childMap.put("handle", child.getHandle());
                        childMap.put("description", child.getDescription());
                        childMap.put("is_internal", child.getIsInternal());
                        childMap.put("is_active", child.getIsActive());
                        childMap.put("rank", child.getRank());
                        childMap.put("created_at", child.getCreatedAt());
                        childMap.put("updated_at", child.getUpdatedAt());
                        childMap.put("deleted_at", null);
                        return childMap;
                    })
                    .collect(Collectors.toList()));

            // 构造最终响应
            Map<String, Object> response = new HashMap<>();
            response.put("product_category", formatted);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("更新分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            // 根据 Medusa 工作流，先检查分类是否存在
            Category category = categoryService.getCategory(id);
            
            // 检查是否有子分类
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // 执行删除操作
            categoryService.deleteCategory(id);
            
            return ResponseEntity.ok().build();
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("删除分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 