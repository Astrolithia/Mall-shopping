package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.ProductRequest;
import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.model.ProductStatus;
import com.qvtu.mallshopping.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> listProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false)ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Product> products = productService.listProducts(title, status, page, size);
        return ResponseEntity.ok(products);
    }
}
