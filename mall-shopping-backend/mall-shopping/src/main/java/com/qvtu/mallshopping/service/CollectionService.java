package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.CollectionCreateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Collection;
import com.qvtu.mallshopping.repository.CollectionRepository;
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

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
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

    public Collection getCollection(Long id) {
        System.out.println("正在查询系列，ID: " + id);
        try {
            Collection collection = collectionRepository.findById(id)
                    .orElseThrow(() -> {
                        System.err.println("系列不存在，ID: " + id);
                        return new ResourceNotFoundException("系列不存在: " + id);
                    });
            System.out.println("查询成功: " + collection);
            return collection;
        } catch (Exception e) {
            System.err.println("查询系列失败: " + e.getMessage());
            throw e;
        }
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

    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("系列不存在: " + id));
        collectionRepository.delete(collection);
    }
}