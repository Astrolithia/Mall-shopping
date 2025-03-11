package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.CategoryCreateRequest;
import com.qvtu.mallshopping.dto.CategoryResponseDTO;
import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.repository.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

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
        return convertToDTO(category, 1); // 默认只获取一层子分类
    }

    private CategoryResponseDTO convertToDTO(Category category, int depth) {
        if (category == null || depth < 0) {
            return null;
        }

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
        
        // 只在需要时转换子分类，并限制递归深度
        if (depth > 0 && category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                .map(child -> convertToDTO(child, depth - 1))
                .collect(Collectors.toList()));
        } else {
            dto.setChildren(null);
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

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product category", "id", id.toString()));
    }

    public Page<Category> getTopLevelCategories(Pageable pageable) {
        return categoryRepository.findByParentCategoryIsNull(pageable);
    }

    public Page<Category> getCategoriesByParent(Long parentId, Pageable pageable) {
        Category parentCategory = getCategory(parentId);
        return categoryRepository.findByParentCategory(parentCategory, pageable);
    }

    public Page<Category> searchCategories(String name, Pageable pageable) {
        return categoryRepository.findByNameContaining(name, pageable);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryCreateRequest request) {
        Category category = getCategory(id);
        
        // 如果handle改变了，检查新handle是否已存在
        if (request.getHandle() != null && !request.getHandle().equals(category.getHandle()) 
                && categoryRepository.existsByHandle(request.getHandle())) {
            throw new RuntimeException("分类Handle已存在");
        }

        // 更新基本信息
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getHandle() != null) {
            category.setHandle(request.getHandle());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIsInternal() != null) {
            category.setIsInternal(request.getIsInternal());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        if (request.getRank() != null) {
            category.setRank(request.getRank());
        }
        if (request.getMetadata() != null) {
            category.setMetadata(request.getMetadata());
        }

        // 更新父分类
        if (request.getParentCategoryId() != null) {
            if (!request.getParentCategoryId().equals(category.getParentCategoryId())) {
                Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                        .orElseThrow(() -> new RuntimeException("父分类不存在"));
                category.setParentCategory(parentCategory);
            }
        } else {
            category.setParentCategory(null);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        categoryRepository.delete(category);
    }
}
