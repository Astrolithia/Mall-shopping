package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.CategoryCreateRequest;
import com.qvtu.mallshopping.dto.CategoryResponseDTO;
import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.repository.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponseDTO> listCategories(String name, int page, int size) {
        // 确保分页参数合理
        page = Math.max(0, page);
        size = Math.max(1, size);
        
        Pageable pageable = PageRequest.of(page, size);
        List<Category> categories;
        
        if (name != null && !name.isEmpty()) {
            categories = categoryRepository.findByNameContaining(name, pageable).getContent();
        } else {
            categories = categoryRepository.findByParentCategoryIsNull(pageable).getContent();
        }
        
        return categories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<CategoryResponseDTO> getChildCategories(Long parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("父分类不存在"));
        List<Category> categories = categoryRepository.findByParentCategory(parentCategory, pageable).getContent();
        
        return categories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public CategoryResponseDTO convertToDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setHandle(category.getHandle());
        dto.setDescription(category.getDescription());
        dto.setIsInternal(category.getIsInternal());
        dto.setIsActive(category.getIsActive());
        dto.setRank(category.getRank());
        dto.setParent_category_id(category.getParentCategoryId());
        dto.setCreated_at(category.getCreatedAt());
        dto.setUpdated_at(category.getUpdatedAt());
        dto.setMetadata(category.getMetadata());
        
        // 转换子分类
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public long countCategories(String name) {
        if (name != null && !name.isEmpty()) {
            return categoryRepository.countByNameContaining(name);
        }
        return categoryRepository.count();
    }

    public long countChildCategories(Long parentId) {
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("父分类不存在"));
        return categoryRepository.countByParentCategory(parentCategory);
    }

    @Transactional
    public Category createCategory(CategoryCreateRequest request) {
        // 检查 handle 是否已经存在
        if (request.getHandle() != null && categoryRepository.existsByHandle(request.getHandle())) {
            throw new RuntimeException("分类Handle已存在");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setHandle(request.getHandle());
        category.setDescription(request.getDescription());
        category.setIsInternal(request.getIsInternal());
        category.setIsActive(request.getIsActive());
        category.setRank(request.getRank());
        category.setMetadata(request.getMetadata());

        // 如果有父分类ID，设置父分类
        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("父分类不存在"));
            category.setParentCategory(parentCategory);
        }

        return categoryRepository.save(category);
    }
}
