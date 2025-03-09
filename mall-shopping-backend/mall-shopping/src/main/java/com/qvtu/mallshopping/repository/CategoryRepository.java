package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 查找顶级分类（没有父分类的）
    Page<Category> findByParentCategoryIsNull(Pageable pageable);
    
    // 按名称搜索分类
    Page<Category> findByNameContaining(String name, Pageable pageable);
    
    // 按名称统计分类数量
    long countByNameContaining(String name);
    
    // 检查handle是否存在
    boolean existsByHandle(String handle);
    
    // 根据父分类ID查找子分类
    Page<Category> findByParentCategoryId(Long parentId, Pageable pageable);
    
    // 根据父分类ID统计子分类数量
    long countByParentCategoryId(Long parentId);
}
