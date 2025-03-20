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
    public Map<String, Object> listOrders(int offset, int limit) {
        // 获取分页订单数据
        Page<Order> orderPage = orderRepository.findAll(PageRequest.of(offset/limit, limit));
        
        // 转换订单列表
        List<Map<String, Object>> formattedOrders = orderPage.getContent().stream()
            .map(this::formatOrderResponse)
            .collect(Collectors.toList());

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("orders", formattedOrders);
        response.put("count", orderPage.getTotalElements());
        response.put("offset", offset);
        response.put("limit", limit);
        
        return response;
    }
    
    private Map<String, Object> formatOrderItem(OrderItem item) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", item.getId());
        formatted.put("title", item.getTitle());
        formatted.put("subtitle", item.getSubtitle());
        formatted.put("thumbnail", item.getThumbnail());
        formatted.put("variant_id", item.getVariantId());
        formatted.put("product_id", item.getProductId());
        formatted.put("quantity", item.getQuantity());
        formatted.put("unit_price", item.getUnitPrice());
        formatted.put("created_at", item.getCreatedAt());
        formatted.put("updated_at", item.getUpdatedAt());
        formatted.put("metadata", item.getMetadata());
        formatted.put("total", item.getTotal());
        formatted.put("subtotal", item.getSubtotal());
        return formatted;
    }

    private Map<String, Object> formatPaymentCollection(PaymentCollection pc) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", pc.getId());
        formatted.put("currency_code", pc.getCurrencyCode());
        formatted.put("amount", pc.getAmount());
        formatted.put("status", pc.getStatus());
        
        // 格式化支付提供商
        List<Map<String, Object>> providers = pc.getPaymentProviders().stream()
            .map(pp -> {
                Map<String, Object> providerMap = new HashMap<>();
                providerMap.put("id", pp.getProviderId());
                providerMap.put("is_enabled", pp.getIsEnabled());
                return providerMap;
            })
            .collect(Collectors.toList());
        formatted.put("payment_providers", providers);
        
        return formatted;
    }

    private Map<String, Object> formatShippingMethod(ShippingMethod sm) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", sm.getId());
        formatted.put("name", sm.getName());
        formatted.put("amount", sm.getAmount());
        formatted.put("shipping_option_id", sm.getShippingOptionId());
        formatted.put("created_at", sm.getCreatedAt());
        formatted.put("updated_at", sm.getUpdatedAt());
        return formatted;
    }

    private Map<String, Object> formatOrderResponse(Order order) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", order.getId());
        formatted.put("status", order.getStatus());
        formatted.put("payment_status", order.getPaymentStatus());
        formatted.put("fulfillment_status", order.getFulfillmentStatus());
        formatted.put("customer_id", order.getCustomerId());
        formatted.put("email", order.getEmail());
        formatted.put("currency_code", order.getCurrencyCode());
        formatted.put("created_at", order.getCreatedAt());
        formatted.put("updated_at", order.getUpdatedAt());
        
        // 格式化订单项
        List<Map<String, Object>> items = order.getItems().stream()
            .map(this::formatOrderItem)
            .collect(Collectors.toList());
        formatted.put("items", items);
        
        // 格式化支付集合
        List<Map<String, Object>> paymentCollections = order.getPaymentCollections().stream()
            .map(this::formatPaymentCollection)
            .collect(Collectors.toList());
        formatted.put("payment_collections", paymentCollections);
        
        // 格式化配送方式
        List<Map<String, Object>> shippingMethods = order.getShippingMethods().stream()
            .map(this::formatShippingMethod)
            .collect(Collectors.toList());
        formatted.put("shipping_methods", shippingMethods);
        
        // 添加订单汇总信息
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", order.getTotal());
        summary.put("subtotal", order.getSubtotal());
        summary.put("tax_total", order.getTaxTotal());
        summary.put("shipping_total", order.getShippingTotal());
        summary.put("discount_total", order.getDiscountTotal());
        formatted.put("summary", summary);

        return formatted;
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
        
        // 设置金额相关字段
        dto.setItemTotal(order.getItemTotal());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxTotal(order.getTaxTotal());
        dto.setShippingTotal(order.getShippingTotal());
        dto.setDiscountTotal(order.getDiscountTotal());
        dto.setTotal(order.getTotal());
        
        // 设置时间戳
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

    @Transactional(readOnly = true)
    public Map<String, Object> getOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Map<String, Object> orderMap = new HashMap<>();
        
        // 基本信息
        orderMap.put("id", order.getId().toString());
        orderMap.put("version", order.getVersion());
        orderMap.put("region_id", order.getRegionId());
        orderMap.put("customer_id", order.getCustomerId());
        orderMap.put("sales_channel_id", order.getSalesChannelId());
        orderMap.put("email", order.getEmail());
        orderMap.put("currency_code", order.getCurrencyCode());
        orderMap.put("payment_status", order.getPaymentStatus().toString());
        orderMap.put("fulfillment_status", order.getFulfillmentStatus().toString());
        orderMap.put("status", order.getStatus().toString());
        
        // 支付集合
        if (order.getPaymentCollections() != null) {
            List<Map<String, Object>> paymentCollections = order.getPaymentCollections().stream()
                .map(pc -> {
                    Map<String, Object> pcMap = new HashMap<>();
                    pcMap.put("id", pc.getId().toString());
                    pcMap.put("currency_code", pc.getCurrencyCode());
                    pcMap.put("amount", pc.getAmount());
                    pcMap.put("status", pc.getStatus());
                    
                    // 支付提供商
                    if (pc.getPaymentProviders() != null) {
                        pcMap.put("payment_providers", pc.getPaymentProviders().stream()
                            .map(pp -> Map.of(
                                "id", pp.getProviderId(),
                                "is_enabled", pp.getIsEnabled()
                            ))
                            .collect(Collectors.toList()));
                    }
                    
                    return pcMap;
                })
                .collect(Collectors.toList());
            orderMap.put("payment_collections", paymentCollections);
        }
        
        // 订单项
        if (order.getItems() != null) {
            List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId().toString());
                    itemMap.put("title", item.getTitle());
                    itemMap.put("subtitle", item.getSubtitle());
                    itemMap.put("thumbnail", item.getThumbnail());
                    itemMap.put("variant_id", item.getVariantId());
                    itemMap.put("product_id", item.getProductId());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unit_price", item.getUnitPrice());
                    itemMap.put("total", item.getTotal());
                    itemMap.put("subtotal", item.getSubtotal());
                    itemMap.put("created_at", item.getCreatedAt());
                    itemMap.put("updated_at", item.getUpdatedAt());
                    itemMap.put("metadata", item.getMetadata());
                    return itemMap;
                })
                .collect(Collectors.toList());
            orderMap.put("items", items);
        }
        
        // 配送方法
        if (order.getShippingMethods() != null) {
            List<Map<String, Object>> shippingMethods = order.getShippingMethods().stream()
                .map(sm -> {
                    Map<String, Object> smMap = new HashMap<>();
                    smMap.put("id", sm.getId().toString());
                    smMap.put("name", sm.getName());
                    smMap.put("amount", sm.getAmount());
                    smMap.put("shipping_option_id", sm.getShippingOptionId());
                    smMap.put("created_at", sm.getCreatedAt());
                    smMap.put("updated_at", sm.getUpdatedAt());
                    return smMap;
                })
                .collect(Collectors.toList());
            orderMap.put("shipping_methods", shippingMethods);
        }
        
        // 订单汇总信息
        if (order.getSummary() != null) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("paid_total", order.getSummary().getPaidTotal());
            summary.put("refunded_total", order.getSummary().getRefundedTotal());
            summary.put("pending_difference", order.getSummary().getPendingDifference());
            summary.put("current_order_total", order.getSummary().getCurrentOrderTotal());
            summary.put("original_order_total", order.getSummary().getOriginalOrderTotal());
            orderMap.put("summary", summary);
        }
        
        // 金额相关
        orderMap.put("item_total", order.getItemTotal());
        orderMap.put("subtotal", order.getSubtotal());
        orderMap.put("tax_total", order.getTaxTotal());
        orderMap.put("shipping_total", order.getShippingTotal());
        orderMap.put("discount_total", order.getDiscountTotal());
        orderMap.put("total", order.getTotal());
        
        // 时间戳
        orderMap.put("created_at", order.getCreatedAt());
        orderMap.put("updated_at", order.getUpdatedAt());
        
        return orderMap;
    }

    @Transactional
    public Map<String, Object> updateOrder(Long id, UpdateOrderRequest request) {
        // 获取订单
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 如果请求体不为空，更新相应字段
        if (request != null) {
            if (request.getEmail() != null) {
                order.setEmail(request.getEmail());
            }
            if (request.getRegionId() != null) {
                order.setRegionId(request.getRegionId());
            }
            if (request.getCustomerId() != null) {
                order.setCustomerId(request.getCustomerId());
            }
            if (request.getSalesChannelId() != null) {
                order.setSalesChannelId(request.getSalesChannelId());
            }
            if (request.getCurrencyCode() != null) {
                order.setCurrencyCode(request.getCurrencyCode());
            }
            if (request.getMetadata() != null) {
                // 如果已有metadata，则合并，否则直接设置
                if (order.getMetadata() != null) {
                    order.getMetadata().putAll(request.getMetadata());
                } else {
                    order.setMetadata(request.getMetadata());
                }
            }
            
            // 更新时间戳
            order.setUpdatedAt(LocalDateTime.now());
            
            // 保存更新
            order = orderRepository.save(order);
        }
        
        // 返回更新后的订单详情
        return getOrder(id);
    }
} 