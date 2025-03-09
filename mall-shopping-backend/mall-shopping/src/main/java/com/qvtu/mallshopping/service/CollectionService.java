package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.CollectionCreateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Collection;
import com.qvtu.mallshopping.model.Product;
import com.qvtu.mallshopping.repository.CollectionRepository;
import com.qvtu.mallshopping.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;

    public CollectionService(CollectionRepository collectionRepository, ProductRepository productRepository) {
        this.collectionRepository = collectionRepository;
        this.productRepository = productRepository;
    }

    public Collection createCollection(CollectionCreateRequest request) {
        if (request.getHandle() != null && collectionRepository.existsByHandle(request.getHandle())) {
            throw new RuntimeException("系列handle已存在");
        }

        Collection collection = new Collection();
        BeanUtils.copyProperties(request, collection);
        return collectionRepository.save(collection);
    }

    public List<Collection> listCollections(String title, int page, int size) {
        // 确保分页参数合理
        page = Math.max(0, page);
        size = Math.max(20, size);
        
        Pageable pageable = PageRequest.of(page, size);
        if (title != null && !title.isEmpty()) {
            return collectionRepository.findByTitleContaining(title, pageable);
        }
        return collectionRepository.findAll(pageable).getContent();
    }

    public long countCollections(String title) {
        if (title != null && !title.isEmpty()) {
            return collectionRepository.countByTitleContaining(title);
        }
        return collectionRepository.count();
    }

    public Collection getCollection(Long id, boolean includeProducts) {
        Collection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("系列不存在: " + id));
        collection.setIncludeProducts(includeProducts);
        return collection;
    }

    public Collection getCollection(Long id) {
        return getCollection(id, false); // 默认不包含产品数据
    }

    public Collection updateCollection(Long id, CollectionCreateRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("系列不存在"));

        if (request.getTitle() != null) {
            collection.setTitle(request.getTitle());
        }
        if (request.getHandle() != null) {
            collection.setHandle(request.getHandle());
        }
        if (request.getDescription() != null) {
            collection.setDescription(request.getDescription());
        }
        if (request.getThumbnail() != null) {
            collection.setThumbnail(request.getThumbnail());
        }
        if (request.getMetadata() != null) {
            collection.setMetadata(request.getMetadata());
        }

        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(Long id) {
        try {
            // 先检查系列是否存在
            Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("系列不存在: " + id));

            // 解除系列与产品的关联
            if (collection.getProducts() != null) {
                collection.getProducts().forEach(product -> {
                    product.setCollection(null);
                    productRepository.save(product);
                });
            }

            // 删除系列
            collectionRepository.delete(collection);

        } catch (Exception e) {
            throw new RuntimeException("删除系列失败: " + e.getMessage(), e);
        }
    }

    public Collection addProductsToCollection(Long collectionId, List<Long> productIds) {
        try {
            System.out.println("=== 开始添加产品到系列 ===");
            System.out.println("系列ID: " + collectionId);
            System.out.println("要添加的产品IDs: " + productIds);
            
            Collection collection = getCollection(collectionId);
            System.out.println("找到系列: " + collection);
            
            List<Product> productsToAdd = productRepository.findAllById(productIds);
            System.out.println("找到的产品: " + productsToAdd);
            
            for (Product product : productsToAdd) {
                System.out.println("处理产品: " + product.getId());
                if (product.getCollection() != null) {
                    System.out.println("产品已在其他系列中，先移除");
                    product.getCollection().removeProduct(product);
                }
                collection.addProduct(product);
                System.out.println("产品添加成功");
            }

            Collection savedCollection = collectionRepository.save(collection);
            System.out.println("保存系列成功: " + savedCollection);
            return savedCollection;
            
        } catch (Exception e) {
            System.err.println("添加产品到系列失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Collection removeProductsFromCollection(Long collectionId, List<Long> productIds) {
        Collection collection = getCollection(collectionId);
        
        List<Product> productsToRemove = productRepository.findAllById(productIds);
        for (Product product : productsToRemove) {
            collection.removeProduct(product);
        }

        return collectionRepository.save(collection);
    }
}