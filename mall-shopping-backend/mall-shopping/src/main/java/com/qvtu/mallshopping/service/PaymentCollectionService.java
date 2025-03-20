package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.PaymentProviderDTO;
import com.qvtu.mallshopping.model.PaymentCollection;
import com.qvtu.mallshopping.model.Order;
import com.qvtu.mallshopping.repository.PaymentCollectionRepository;
import com.qvtu.mallshopping.repository.OrderRepository;
import com.qvtu.mallshopping.repository.PaymentProviderRepository;
import com.qvtu.mallshopping.dto.CreatePaymentCollectionRequest;
import com.qvtu.mallshopping.dto.PaymentCollectionDTO;
import com.qvtu.mallshopping.dto.MarkAsPaidRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import com.qvtu.mallshopping.enums.OrderStatus;
import com.qvtu.mallshopping.enums.PaymentStatus;
import com.qvtu.mallshopping.enums.FulfillmentStatus;

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
        // 获取订单并验证
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        if (order == null) {
            throw new RuntimeException("Order cannot be null");
        }

        // 创建支付集合
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setOrder(order);  // 设置订单关联
        paymentCollection.setCurrencyCode(order.getCurrencyCode());
        paymentCollection.setAmount(order.getTotal());
        paymentCollection.setStatus("awaiting");
        
        // 初始化金额
        paymentCollection.setAuthorizedAmount(BigDecimal.ZERO);
        paymentCollection.setCapturedAmount(BigDecimal.ZERO);
        paymentCollection.setRefundedAmount(BigDecimal.ZERO);
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        paymentCollection.setCreatedAt(now);
        paymentCollection.setUpdatedAt(now);
        
        // 保存并立即刷新
        paymentCollection = paymentCollectionRepository.saveAndFlush(paymentCollection);
        
        // 验证保存后的实体
        if (paymentCollection.getOrder() == null) {
            throw new RuntimeException("Failed to associate order with payment collection");
        }
        
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
            // 先创建一个订单
            Order order = new Order();
            order.setStatus(OrderStatus.draft);
            order.setCurrencyCode("CNY");
            order.setTotal(new BigDecimal("100.00").add(new BigDecimal(i * 100)));
            order.setPaymentStatus(PaymentStatus.not_paid);
            order.setFulfillmentStatus(FulfillmentStatus.not_fulfilled);
            
            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            
            // 保存订单并确保获取到保存后的实例
            order = orderRepository.save(order);
            
            // 创建关联的支付集合
            PaymentCollection paymentCollection = new PaymentCollection();
            paymentCollection.setOrder(order);  // 确保设置了订单
            paymentCollection.setCurrencyCode(order.getCurrencyCode());
            paymentCollection.setAmount(order.getTotal());
            paymentCollection.setStatus("awaiting");
            paymentCollection.setCreatedAt(now);
            paymentCollection.setUpdatedAt(now);
            
            // 保存支付集合
            paymentCollection = paymentCollectionRepository.save(paymentCollection);
            
            // 验证关联是否正确
            if (paymentCollection.getOrder() == null) {
                throw new RuntimeException("Failed to associate order with payment collection");
            }
            
            results.add(convertToDTO(paymentCollection));
        }
        
        return results;
    }

    @Transactional
    public PaymentCollectionDTO generateRandomPaymentCollection() {
        // 先创建一个订单
        Order order = new Order();
        order.setStatus(OrderStatus.draft);
        order.setCurrencyCode("CNY");
        order.setTotal(new BigDecimal(new Random().nextInt(1000000) / 100.0));
        order.setPaymentStatus(PaymentStatus.not_paid);
        order.setFulfillmentStatus(FulfillmentStatus.not_fulfilled);
        
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        // 保存订单并确保获取到保存后的实例
        order = orderRepository.save(order);
        
        // 创建关联的支付集合
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setOrder(order);  // 确保设置了订单
        paymentCollection.setCurrencyCode(order.getCurrencyCode());
        paymentCollection.setAmount(order.getTotal());
        paymentCollection.setStatus("awaiting");
        paymentCollection.setCreatedAt(now);
        paymentCollection.setUpdatedAt(now);
        
        // 保存支付集合
        paymentCollection = paymentCollectionRepository.save(paymentCollection);
        
        // 验证关联是否正确
        if (paymentCollection.getOrder() == null) {
            throw new RuntimeException("Failed to associate order with payment collection");
        }
        
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

    @Transactional
    public PaymentCollectionDTO markAsPaid(Long id, MarkAsPaidRequest request) {
        // 验证请求参数
        if (request == null) {
            throw new RuntimeException("Request cannot be null");
        }
        if (request.getOrderId() == null) {
            throw new RuntimeException("Order ID is required in the request");
        }

        // 获取支付集合
        PaymentCollection paymentCollection = paymentCollectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment collection not found with id: " + id));
        
        // 验证订单ID，添加更详细的错误信息
        Long paymentCollectionOrderId = paymentCollection.getOrder().getId();
        if (!paymentCollectionOrderId.equals(request.getOrderId())) {
            throw new RuntimeException(String.format(
                "Order ID mismatch. Payment collection's order ID: %d, Request order ID: %d",
                paymentCollectionOrderId,
                request.getOrderId()
            ));
        }
        
        // 检查当前状态
        if ("paid".equals(paymentCollection.getStatus())) {
            throw new RuntimeException("Payment collection is already paid");
        }
        
        // 更新状态为已付款
        paymentCollection.setStatus("paid");
        paymentCollection.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        paymentCollection = paymentCollectionRepository.save(paymentCollection);
        
        // 返回更新后的DTO
        return convertToDTO(paymentCollection);
    }
} 