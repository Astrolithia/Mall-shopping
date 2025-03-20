package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.PaymentProviderDTO;
import com.qvtu.mallshopping.model.PaymentCollection;
import com.qvtu.mallshopping.model.Order;
import com.qvtu.mallshopping.repository.PaymentCollectionRepository;
import com.qvtu.mallshopping.repository.OrderRepository;
import com.qvtu.mallshopping.repository.PaymentProviderRepository;
import com.qvtu.mallshopping.dto.CreatePaymentCollectionRequest;
import com.qvtu.mallshopping.dto.PaymentCollectionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Service
public class PaymentCollectionService {
    private final PaymentCollectionRepository paymentCollectionRepository;
    private final OrderRepository orderRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    
    public PaymentCollectionService(
        PaymentCollectionRepository paymentCollectionRepository,
        OrderRepository orderRepository,
        PaymentProviderRepository paymentProviderRepository
    ) {
        this.paymentCollectionRepository = paymentCollectionRepository;
        this.orderRepository = orderRepository;
        this.paymentProviderRepository = paymentProviderRepository;
    }
    
    @Transactional
    public PaymentCollectionDTO createPaymentCollection(CreatePaymentCollectionRequest request) {
        // 获取订单
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 创建支付集合
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setOrder(order);
        paymentCollection.setCurrencyCode(order.getCurrencyCode());
        paymentCollection.setAmount(order.getTotal());
        paymentCollection.setStatus("awaiting");  // 初始状态为等待中
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        paymentCollection.setCreatedAt(now);
        paymentCollection.setUpdatedAt(now);
        
        // 保存支付集合
        paymentCollection = paymentCollectionRepository.save(paymentCollection);
        
        // 转换为DTO
        return convertToDTO(paymentCollection);
    }
    
    private PaymentCollectionDTO convertToDTO(PaymentCollection pc) {
        PaymentCollectionDTO dto = new PaymentCollectionDTO();
        dto.setId(pc.getId().toString());
        dto.setCurrencyCode(pc.getCurrencyCode());
        dto.setAmount(pc.getAmount());
        dto.setStatus(pc.getStatus());
        
        // 修改获取支付提供商的逻辑
        dto.setPaymentProviders(
            paymentProviderRepository.findByIsEnabled(true).stream()
                .map(pp -> {
                    PaymentProviderDTO ppDto = new PaymentProviderDTO();
                    ppDto.setId(pp.getProviderId());
                    ppDto.setIsEnabled(true);
                    return ppDto;
                })
                .collect(Collectors.toList())
        );
        
        return dto;
    }

    @Transactional
    public void deletePaymentCollection(Long id) {
        PaymentCollection paymentCollection = paymentCollectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment collection not found"));
        
        // 检查支付集合状态，只有某些状态下才能删除
        if ("captured".equals(paymentCollection.getStatus()) || 
            "authorized".equals(paymentCollection.getStatus())) {
            throw new RuntimeException("Cannot delete payment collection in current status");
        }
        
        // 删除支付集合
        paymentCollectionRepository.delete(paymentCollection);
    }

    @Transactional
    public List<PaymentCollectionDTO> seedPaymentCollections() {
        List<PaymentCollectionDTO> results = new ArrayList<>();
        
        // 生成5个测试付款集合
        for (int i = 0; i < 5; i++) {
            PaymentCollection paymentCollection = new PaymentCollection();
            paymentCollection.setCurrencyCode("CNY");
            paymentCollection.setAmount(new BigDecimal("100.00").add(new BigDecimal(i * 100)));
            paymentCollection.setStatus("awaiting");
            
            LocalDateTime now = LocalDateTime.now();
            paymentCollection.setCreatedAt(now);
            paymentCollection.setUpdatedAt(now);
            
            paymentCollection = paymentCollectionRepository.save(paymentCollection);
            results.add(convertToDTO(paymentCollection));
        }
        
        return results;
    }

    @Transactional
    public PaymentCollectionDTO generateRandomPaymentCollection() {
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCurrencyCode("CNY");
        paymentCollection.setAmount(new BigDecimal(new Random().nextInt(1000000) / 100.0));
        paymentCollection.setStatus("awaiting");
        
        LocalDateTime now = LocalDateTime.now();
        paymentCollection.setCreatedAt(now);
        paymentCollection.setUpdatedAt(now);
        
        paymentCollection = paymentCollectionRepository.save(paymentCollection);
        return convertToDTO(paymentCollection);
    }

    @Transactional
    public List<PaymentCollectionDTO> generateRandomPaymentCollections(int count) {
        List<PaymentCollectionDTO> results = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            results.add(generateRandomPaymentCollection());
        }
        
        return results;
    }
} 