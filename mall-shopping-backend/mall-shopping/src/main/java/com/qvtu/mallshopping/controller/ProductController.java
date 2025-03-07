package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.model.ProductOption;
import com.qvtu.mallshopping.enums.ProductStatus;
import com.qvtu.mallshopping.model.ProductVariant;
import com.qvtu.mallshopping.service.ProductService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductCreateRequest request) {
        try {
            Product product = productService.createProduct(request);
            return ResponseEntity.ok(convertToDTO(product));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setSubtitle(product.getSubtitle());
        dto.setDescription(product.getDescription());
        dto.setThumbnail(product.getThumbnail());
        dto.setHandle(product.getHandle());
        dto.setStatus(product.getStatus().toString());

        // 转换选项
        if (product.getOptions() != null) {
            dto.setOptions(product.getOptions().stream()
                    .map(this::convertOptionToDTO)
                    .collect(Collectors.toList()));
        }

        // 转换变体
        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::convertVariantToDTO)
                    .collect(Collectors.toList()));
        }

        // 设置其他基本信息
        dto.setWeight(product.getWeight());
        dto.setLength(product.getLength());
        dto.setHeight(product.getHeight());
        dto.setWidth(product.getWidth());

        // 设置时间，如果时间为null则不设置
        if (product.getCreatedAt() != null) {
            dto.setCreatedAt(product.getCreatedAt().toString());
        }
        if (product.getUpdatedAt() != null) {
            dto.setUpdatedAt(product.getUpdatedAt().toString());
        }

        return dto;
    }

    private ProductOptionResponseDTO convertOptionToDTO(ProductOption option) {
        ProductOptionResponseDTO dto = new ProductOptionResponseDTO();
        dto.setId(option.getId());
        dto.setTitle(option.getTitle());
        // 设置选项值
        return dto;
    }

    private ProductVariantResponseDTO convertVariantToDTO(ProductVariant variant) {
        ProductVariantResponseDTO dto = new ProductVariantResponseDTO();
        dto.setId(variant.getId());
        dto.setTitle(variant.getTitle());
        dto.setSku(variant.getSku());
        // 转换价格信息
        return dto;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Product> products = productService.listProducts(title, status, page, size);
        long total = productService.countProducts(title, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("count", total);
        response.put("offset", page * size);
        response.put("limit", size);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long id,
            @RequestBody ProductStatusRequest request
    ) {
        try {
            Product updateProduct = productService.updateProductStatus(id, request.getStatus());
            return ResponseEntity.ok(updateProduct);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

@Data
class ProductStatusRequest {
    private String status;
}