package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.repository.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listCategories(String name, int page, int size) {
        // 确保分页参数合理
        page = Math.max(0, page);
        size = Math.max(1, size);
        
        Pageable pageable = PageRequest.of(page, size);
        if (name != null && !name.isEmpty()) {
            return categoryRepository.findByNameContaining(name, pageable).getContent();
        }
        return categoryRepository.findByParentCategoryIsNull(pageable).getContent();
    }

    public long countCategories(String name) {
        if (name != null && !name.isEmpty()) {
            return categoryRepository.countByNameContaining(name);
        }
        return categoryRepository.count();
    }

    public List<Category> getChildCategories(Long parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findByParentCategoryId(parentId, pageable).getContent();
    }

    public long countChildCategories(Long parentId) {
        return categoryRepository.countByParentCategoryId(parentId);
    }
}
