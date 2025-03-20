package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.model.*;
import com.qvtu.mallshopping.repository.OrderRepository;
import com.qvtu.mallshopping.repository.ShippingMethodRepository;
import com.qvtu.mallshopping.enums.OrderStatus;
import com.qvtu.mallshopping.enums.PaymentStatus;
import com.qvtu.mallshopping.enums.FulfillmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    
    public OrderService(
        OrderRepository orderRepository,
        ShippingMethodRepository shippingMethodRepository
    ) {
        this.orderRepository = orderRepository;
        this.shippingMethodRepository = shippingMethodRepository;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> listOrders(Integer offset, Integer limit) {
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );
        
        Page<Order> ordersPage = orderRepository.findAll(pageRequest);
        
        List<OrderDTO> orderDTOs = ordersPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
            
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("count", ordersPage.getTotalElements());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);
        
        return response;
    }
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId().toString());
        dto.setVersion(order.getVersion());
        dto.setRegionId(order.getRegionId());
        dto.setCustomerId(order.getCustomerId());
        dto.setSalesChannelId(order.getSalesChannelId());
        dto.setEmail(order.getEmail());
        dto.setCurrencyCode(order.getCurrencyCode());
        dto.setPaymentStatus(order.getPaymentStatus().toString());
        dto.setFulfillmentStatus(order.getFulfillmentStatus().toString());
        dto.setStatus(order.getStatus());
        dto.setItemTotal(order.getItemTotal());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxTotal(order.getTaxTotal());
        dto.setShippingTotal(order.getShippingTotal());
        dto.setDiscountTotal(order.getDiscountTotal());
        dto.setTotal(order.getTotal());
        dto.setMetadata(order.getMetadata());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        return dto;
    }
    
    @Transactional
    public Order createDraftOrder(CreateDraftOrderRequest request) {
        Order order = new Order();
        
        // 设置基本信息
        order.setStatus(OrderStatus.draft);
        order.setEmail(request.getEmail());
        order.setCustomerId(request.getCustomerId());
        order.setRegionId(request.getRegionId());
        order.setSalesChannelId(request.getSalesChannelId());
        order.setCurrencyCode(request.getCurrencyCode());
        order.setMetadata(request.getMetadata());
        
        // 设置初始状态
        order.setPaymentStatus(PaymentStatus.not_paid);
        order.setFulfillmentStatus(FulfillmentStatus.not_fulfilled);
        
        // 设置初始金额
        order.setItemTotal(BigDecimal.ZERO);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTaxTotal(BigDecimal.ZERO);
        order.setShippingTotal(BigDecimal.ZERO);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        // 保存订单
        final Order savedOrder = orderRepository.save(order);
        
        // 处理配送方式
        if (request.getShippingMethods() != null) {
            List<ShippingMethod> shippingMethods = request.getShippingMethods().stream()
                .map(sm -> {
                    ShippingMethod shippingMethod = new ShippingMethod();
                    shippingMethod.setOrder(savedOrder);
                    shippingMethod.setName(sm.getName());
                    shippingMethod.setShippingOptionId(sm.getOptionId());
                    return shippingMethod;
                })
                .collect(Collectors.toList());
            
            shippingMethodRepository.saveAll(shippingMethods);
        }
        
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public OrderDTO getDraftOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Draft order not found"));
            
        // 验证是否为草稿订单
        if (order.getStatus() != OrderStatus.draft) {
            throw new RuntimeException("Order is not a draft order");
        }
        
        // 转换为 DTO 并返回
        OrderDTO dto = convertToDTO(order);
        
        // 加载关联数据
        if (order.getPaymentCollections() != null) {
            dto.setPaymentCollections(order.getPaymentCollections().stream()
                .map(this::convertPaymentCollectionToDTO)
                .collect(Collectors.toList()));
        }
        
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList()));
        }
        
        if (order.getShippingMethods() != null) {
            dto.setShippingMethods(order.getShippingMethods().stream()
                .map(this::convertShippingMethodToDTO)
                .collect(Collectors.toList()));
        }
        
        if (order.getSummary() != null) {
            dto.setSummary(convertOrderSummaryToDTO(order.getSummary()));
        }
        
        return dto;
    }

    private PaymentCollectionDTO convertPaymentCollectionToDTO(PaymentCollection pc) {
        PaymentCollectionDTO dto = new PaymentCollectionDTO();
        dto.setId(pc.getId().toString());
        dto.setCurrencyCode(pc.getCurrencyCode());
        dto.setAmount(pc.getAmount());
        dto.setStatus(pc.getStatus());
        
        if (pc.getPaymentProviders() != null) {
            dto.setPaymentProviders(pc.getPaymentProviders().stream()
                .map(pp -> {
                    PaymentProviderDTO ppDto = new PaymentProviderDTO();
                    ppDto.setId(pp.getProviderId());
                    ppDto.setIsEnabled(pp.getIsEnabled());
                    return ppDto;
                })
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId().toString());
        dto.setTitle(item.getTitle());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        // ... 设置其他字段
        return dto;
    }

    private ShippingMethodDTO convertShippingMethodToDTO(ShippingMethod sm) {
        ShippingMethodDTO dto = new ShippingMethodDTO();
        dto.setId(sm.getId().toString());
        dto.setName(sm.getName());
        dto.setAmount(sm.getAmount());
        // ... 设置其他字段
        return dto;
    }

    private OrderSummaryDTO convertOrderSummaryToDTO(OrderSummary summary) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setPaidTotal(summary.getPaidTotal());
        dto.setRefundedTotal(summary.getRefundedTotal());
        // ... 设置其他字段
        return dto;
    }
} 