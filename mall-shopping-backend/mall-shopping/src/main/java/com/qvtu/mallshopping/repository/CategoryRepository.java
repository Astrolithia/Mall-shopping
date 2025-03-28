package com.qvtu.mallshopping.repository;

import com.qvtu.mallshopping.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 查找顶级分类（没有父分类的）
    Page<Category> findByParentCategoryIsNull(Pageable pageable);
    
    // 根据父分类查找子分类
    Page<Category> findByParentCategory(Category parentCategory, Pageable pageable);
    
    // 根据名称搜索分类
    Page<Category> findByNameContaining(String name, Pageable pageable);
    
    // 按名称统计分类数量
    long countByNameContaining(String name);
    
    // 检查handle是否存在
    boolean existsByHandle(String handle);
    
    // 根据父分类统计子分类数量
    long countByParentCategory(Category parentCategory);
}
