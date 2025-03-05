package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.ProductRequest;
import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.model.ProductStatus;
import com.qvtu.mallshopping.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


@Service
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
}
