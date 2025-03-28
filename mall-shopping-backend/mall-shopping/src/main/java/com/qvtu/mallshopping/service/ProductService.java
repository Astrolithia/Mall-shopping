package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.model.ProductOption;
import com.qvtu.mallshopping.enums.ProductStatus;
import com.qvtu.mallshopping.model.ProductVariant;
import com.qvtu.mallshopping.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;


import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(ProductRequest request) {
        if (productRepository.existsByHandle(request.getHandle())) {
            throw new RuntimeException("商品handle已存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setStatus(ProductStatus.DRAFT); // 新创建的商品默认为草稿状态

        return productRepository.save(product);
    }

    public List<Product> listProducts(String title, ProductStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (title != null && status != null) {
            return productRepository.findByTitleContainingAndStatus(title, status, pageable);
        } else if (title != null) {
            return productRepository.findByTitleContaining(title, pageable);
        } else if (status != null) {
            return productRepository.findByStatus(status, pageable);
        } else {
            return productRepository.findAll(pageable).getContent();
        }
    }

    public long countProducts(String title, ProductStatus status) {
        if (title != null && status != null) {
            return productRepository.countByTitleContainingAndStatus(title, status);
        } else if (title != null) {
            return productRepository.countByTitleContaining(title);
        } else if (status != null) {
            return productRepository.countByStatus(status);
        } else {
            return productRepository.count();
        }
    }

    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 更新基本信息
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getSubtitle() != null) {
            product.setSubtitle(request.getSubtitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getHandle() != null) {
            product.setHandle(request.getHandle());
        }

        // 更新状态
        if (request.getStatus() != null) {
            try {
                ProductStatus newStatus = ProductStatus.valueOf(request.getStatus().toUpperCase());
                product.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的商品状态: " + request.getStatus());
            }
        }
        
        // 保存更新
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
    }

    public Product createProduct(ProductCreateRequest request) {
        // 验证handle唯一性
        if (request.getHandle() != null && productRepository.existsByHandle(request.getHandle())) {
            throw new RuntimeException("商品handle已存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(request, product);

        // 设置状态
        product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));

        // 处理选项
        if (request.getOptions() != null) {
            for (ProductOptionDTO optionDTO : request.getOptions()) {
                ProductOption option = new ProductOption();
                option.setTitle(optionDTO.getTitle());
                option.setProduct(product);
                product.getOptions().add(option);
            }
        }

        // 处理变体
        if (request.getVariants() != null) {
            for (ProductVariantDTO variantDTO : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                BeanUtils.copyProperties(variantDTO, variant);
                variant.setProduct(product);
                product.getVariants().add(variant);
            }
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        // 检查商品是否存在
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在: " + id));

        // 直接物理删除
        productRepository.delete(product);
    }


    public Product updateProductStatus(Long id, String status) {
        // 查找商品
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在： " + id));

        try {
            // 转换状态字符串为枚举（忽略大小写）
            ProductStatus newStatus = ProductStatus.valueOf(status.toUpperCase());

            // 更新状态
            product.setStatus(newStatus);

            // 保存更新
            return productRepository.save(product);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的商品状态： " + status);
        }
    }
}
