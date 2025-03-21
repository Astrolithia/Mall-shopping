package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.*;
import com.qvtu.mallshopping.model.*;
import com.qvtu.mallshopping.repository.OrderRepository;
import com.qvtu.mallshopping.repository.ShippingMethodRepository;
import com.qvtu.mallshopping.repository.OrderChangeRepository;
import com.qvtu.mallshopping.enums.OrderStatus;
import com.qvtu.mallshopping.enums.PaymentStatus;
import com.qvtu.mallshopping.enums.FulfillmentStatus;
import com.qvtu.mallshopping.enums.PaymentCollectionStatus;
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
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final OrderChangeRepository orderChangeRepository;
    
    public OrderService(
        OrderRepository orderRepository,
        ShippingMethodRepository shippingMethodRepository,
        OrderChangeRepository orderChangeRepository
    ) {
        this.orderRepository = orderRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.orderChangeRepository = orderChangeRepository;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> listOrders(int offset, int limit) {
        log.debug("Listing orders with offset: {} and limit: {}", offset, limit);
        try {
            // 获取分页订单数据
            Page<Order> orderPage = orderRepository.findAll(PageRequest.of(offset/limit, limit));
            log.debug("Found {} orders", orderPage.getTotalElements());
            
            // 转换订单列表
            List<Map<String, Object>> formattedOrders = orderPage.getContent().stream()
                .map(order -> {
                    try {
                        log.debug("Formatting order: {}", order.getId());
                        return formatOrderResponse(order);
                    } catch (Exception e) {
                        log.error("Error formatting order {}: {}", order.getId(), e.getMessage());
                        throw new RuntimeException("Error formatting order", e);
                    }
                })
                .collect(Collectors.toList());

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("orders", formattedOrders);
            response.put("count", orderPage.getTotalElements());
            response.put("offset", offset);
            response.put("limit", limit);
            
            return response;
        } catch (Exception e) {
            log.error("Error listing orders: ", e);
            throw e;
        }
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
        formatted.put("status", pc.getStatus().getValue());
        
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
        log.debug("Formatting order response for order ID: {}", order.getId());
        try {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("id", order.getId());
            
            // 添加状态前的日志和空值检查
            OrderStatus status = order.getStatus();
            log.debug("Order {} status before formatting: {}", order.getId(), status);
            formatted.put("status", status != null ? status.name() : null);
            
            PaymentStatus paymentStatus = order.getPaymentStatus();
            log.debug("Order {} payment status before formatting: {}", order.getId(), paymentStatus);
            formatted.put("payment_status", paymentStatus != null ? paymentStatus.name() : null);
            
            FulfillmentStatus fulfillmentStatus = order.getFulfillmentStatus();
            log.debug("Order {} fulfillment status before formatting: {}", order.getId(), fulfillmentStatus);
            formatted.put("fulfillment_status", fulfillmentStatus != null ? fulfillmentStatus.name() : null);
            
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
        } catch (Exception e) {
            log.error("Error formatting order {}: ", order.getId(), e);
            throw new RuntimeException("Error formatting order: " + e.getMessage(), e);
        }
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
        order.setStatus(OrderStatus.DRAFT);
        order.setEmail(request.getEmail());
        order.setCustomerId(request.getCustomerId());
        order.setRegionId(request.getRegionId());
        order.setSalesChannelId(request.getSalesChannelId());
        order.setCurrencyCode(request.getCurrencyCode());
        order.setMetadata(request.getMetadata());
        
        // 设置初始状态
        order.setPaymentStatus(PaymentStatus.PENDING);
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
        if (order.getStatus() != OrderStatus.DRAFT) {
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
        dto.setStatus(pc.getStatus().getValue());
        
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
        log.debug("Getting order with ID: {}", id);
        try {
            Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            log.debug("Found order: {}", order);
            log.debug("Order status: {}", order.getStatus());
            log.debug("Order payment status: {}", order.getPaymentStatus());
            log.debug("Order fulfillment status: {}", order.getFulfillmentStatus());
            
            return formatOrderResponse(order);
        } catch (Exception e) {
            log.error("Error getting order {}: ", id, e);
            throw e;
        }
    }

    @Transactional
    public Map<String, Object> updateOrder(Long id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        System.out.println("============ DEBUG INFO ============");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Current order status: " + order.getStatus());
        System.out.println("Current payment status: " + order.getPaymentStatus());
        System.out.println("Current fulfillment status: " + order.getFulfillmentStatus());
        System.out.println("Request content: " + request);
        
        if (request != null) {
            if (request.getPaymentStatus() != null) {
                try {
                    System.out.println("\nProcessing payment status update:");
                    System.out.println("Raw payment status from request: " + request.getPaymentStatus());
                    String paymentStatusStr = request.getPaymentStatus().toLowerCase();
                    System.out.println("Converted to lowercase: " + paymentStatusStr);
                    System.out.println("Available payment statuses: " + Arrays.toString(PaymentStatus.values()));
                    PaymentStatus newPaymentStatus = PaymentStatus.valueOf(paymentStatusStr);
                    System.out.println("Successfully converted to enum: " + newPaymentStatus);
                    
                    System.out.println("Before setting payment status: " + order.getPaymentStatus());
                    order.setPaymentStatus(newPaymentStatus);
                    System.out.println("After setting payment status: " + order.getPaymentStatus());
                } catch (Exception e) {
                    System.out.println("Failed to update payment status: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Invalid payment status: " + request.getPaymentStatus() 
                        + ". Available values are: " + Arrays.toString(PaymentStatus.values()));
                }
            }
            
            if (request.getFulfillmentStatus() != null) {
                try {
                    System.out.println("\nProcessing fulfillment status update:");
                    System.out.println("Raw fulfillment status from request: " + request.getFulfillmentStatus());
                    String fulfillmentStatusStr = request.getFulfillmentStatus().toLowerCase();
                    System.out.println("Converted to lowercase: " + fulfillmentStatusStr);
                    System.out.println("Available fulfillment statuses: " + Arrays.toString(FulfillmentStatus.values()));
                    FulfillmentStatus newFulfillmentStatus = FulfillmentStatus.valueOf(fulfillmentStatusStr);
                    System.out.println("Successfully converted to enum: " + newFulfillmentStatus);
                    order.setFulfillmentStatus(newFulfillmentStatus);
                    System.out.println("New fulfillment status set: " + order.getFulfillmentStatus());
                } catch (IllegalArgumentException e) {
                    System.out.println("Failed to update fulfillment status: " + e.getMessage());
                    System.out.println("Available fulfillment statuses: " + Arrays.toString(FulfillmentStatus.values()));
                    throw new RuntimeException("Invalid fulfillment status: " + request.getFulfillmentStatus() 
                        + ". Available values are: " + Arrays.toString(FulfillmentStatus.values()));
                }
            }
            
            order.setUpdatedAt(LocalDateTime.now());
            try {
                System.out.println("\nSaving order to database...");
                System.out.println("Payment status before save: " + order.getPaymentStatus());
                order = orderRepository.save(order);
                System.out.println("Order saved successfully");
                System.out.println("Payment status after save: " + order.getPaymentStatus());
                
                Order savedOrder = orderRepository.findById(order.getId()).orElse(null);
                if (savedOrder != null) {
                    System.out.println("Re-fetched order payment status: " + savedOrder.getPaymentStatus());
                }
            } catch (Exception e) {
                System.out.println("Failed to save order: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        
        System.out.println("============ END DEBUG INFO ============");
        return getOrder(id);
    }

    @Transactional
    public Map<String, Object> archiveOrder(Long id, ArchiveOrderRequest request) {
        // 验证请求参数
        if (request == null || !id.equals(request.getOrderId())) {
            throw new RuntimeException("Order ID mismatch");
        }

        // 获取订单
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 检查订单状态是否允许存档
        if (order.getStatus() == OrderStatus.ARCHIVED) {
            throw new RuntimeException("Order is already archived");
        }
        
        // 验证订单是否可以被存档（例如：已完成的订单）
        if (order.getStatus() != OrderStatus.COMPLETED && 
            order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException("Only completed or canceled orders can be archived");
        }
        
        // 更新订单状态
        OrderStatus originalStatus = order.getStatus();
        order.setStatus(OrderStatus.ARCHIVED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 创建变更记录
        createOrderChange(order, "Order archived");
        
        // 保存更新
        order = orderRepository.save(order);
        
        // 返回更新后的订单详情
        return getOrder(id);
    }

    @Transactional
    public Map<String, Object> cancelOrder(Long id) {
        // 获取订单
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 检查订单状态是否允许取消
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already canceled");
        }
        
        // 验证订单是否可以被取消（例如：不能取消已完成的订单）
        if (order.getStatus() == OrderStatus.COMPLETED || 
            order.getStatus() == OrderStatus.ARCHIVED) {
            throw new RuntimeException("Completed or archived orders cannot be canceled");
        }
        
        // 更新订单状态
        OrderStatus originalStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        order.setFulfillmentStatus(FulfillmentStatus.canceled);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 创建变更记录
        createOrderChange(order, "Order canceled");
        
        // 取消相关的支付集合
        if (order.getPaymentCollections() != null) {
            order.getPaymentCollections().forEach(pc -> {
                if (pc.getStatus() != PaymentCollectionStatus.CANCELLED) {
                    pc.setStatus(PaymentCollectionStatus.CANCELLED);
                    pc.setUpdatedAt(LocalDateTime.now());
                }
            });
        }
        
        // 保存更新
        order = orderRepository.save(order);
        
        // 返回更新后的订单详情
        return getOrder(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderChanges(Long orderId) {
        // 获取订单变更历史
        List<OrderChange> changes = orderChangeRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        
        // 转换为响应格式
        return changes.stream().map(change -> {
            Map<String, Object> changeMap = new HashMap<>();
            changeMap.put("id", change.getId());
            changeMap.put("version", change.getVersion());
            changeMap.put("order_id", change.getOrderId());
            changeMap.put("return_id", change.getReturnId());
            changeMap.put("exchange_id", change.getExchangeId());
            changeMap.put("claim_id", change.getClaimId());
            changeMap.put("status", change.getStatus());
            changeMap.put("requested_by", change.getRequestedBy());
            changeMap.put("requested_at", change.getRequestedAt());
            changeMap.put("confirmed_by", change.getConfirmedBy());
            changeMap.put("confirmed_at", change.getConfirmedAt());
            changeMap.put("declined_by", change.getDeclinedBy());
            changeMap.put("declined_reason", change.getDeclinedReason());
            changeMap.put("declined_at", change.getDeclinedAt());
            changeMap.put("canceled_by", change.getCanceledBy());
            changeMap.put("canceled_at", change.getCanceledAt());
            changeMap.put("metadata", change.getMetadata());
            changeMap.put("created_at", change.getCreatedAt());
            changeMap.put("updated_at", change.getUpdatedAt());
            
            // 如果需要，可以添加关联的订单、退货、换货等信息
            if (change.getOrderId() != null) {
                changeMap.put("order", getOrder(change.getOrderId()));
            }
            
            return changeMap;
        }).collect(Collectors.toList());
    }

    // 创建订单变更记录的辅助方法
    private OrderChange createOrderChange(Order order, String description) {
        OrderChange change = new OrderChange();
        change.setOrderId(order.getId());
        change.setVersion(order.getVersion());
        change.setStatus(order.getStatus().toString());
        change.setRequestedBy("system"); // 可以从认证上下文中获取当前用户
        change.setRequestedAt(LocalDateTime.now());
        change.setConfirmedBy("system");
        change.setConfirmedAt(LocalDateTime.now());
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", description);
        change.setMetadata(metadata);
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        change.setCreatedAt(now);
        change.setUpdatedAt(now);
        
        // 保存变更记录
        return orderChangeRepository.save(change);
    }

    @Transactional
    public Map<String, Object> completeOrder(Long id, CompleteOrderRequest request) {
        // 获取订单
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 检查订单状态是否允许完成
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Order is already completed");
        }
        
        // 验证订单是否可以被完成
        if (order.getStatus() == OrderStatus.ARCHIVED || 
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Archived or canceled orders cannot be completed");
        }
        
        // 检查支付状态
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Cannot complete order with unpaid payment status");
        }
        
        // 检查配送状态
        if (order.getFulfillmentStatus() != FulfillmentStatus.fulfilled) {
            throw new RuntimeException("Cannot complete order with unfulfilled status");
        }
        
        // 更新订单状态
        OrderStatus originalStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 创建变更记录
        createOrderChange(order, "Order completed");
        
        // 保存更新
        order = orderRepository.save(order);
        
        // 返回更新后的订单详情
        return getOrder(id);
    }

    @Transactional
    public void testEnumConversion(String paymentStatus, String fulfillmentStatus) {
        log.info("Testing enum conversion...");
        
        try {
            PaymentStatus ps = PaymentStatus.valueOf(paymentStatus.toLowerCase());
            log.info("Successfully converted payment status: {} -> {}", paymentStatus, ps);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert payment status: {}", paymentStatus);
            log.error("Available payment statuses: {}", Arrays.toString(PaymentStatus.values()));
        }
        
        try {
            FulfillmentStatus fs = FulfillmentStatus.valueOf(fulfillmentStatus.toLowerCase());
            log.info("Successfully converted fulfillment status: {} -> {}", fulfillmentStatus, fs);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert fulfillment status: {}", fulfillmentStatus);
            log.error("Available fulfillment statuses: {}", Arrays.toString(FulfillmentStatus.values()));
        }
    }

    @Transactional
    public Map<String, Object> fulfillOrder(Long id, FulfillOrderRequest request) {
        // 获取订单
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 验证订单状态
        if (order.getFulfillmentStatus() == FulfillmentStatus.fulfilled) {
            throw new RuntimeException("Order is already fulfilled");
        }
        
        if (order.getFulfillmentStatus() == FulfillmentStatus.canceled) {
            throw new RuntimeException("Cannot fulfill canceled order");
        }
        
        // 验证请求项
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("No items to fulfill");
        }
        
        // 验证并更新每个订单项的配送数量
        for (FulfillOrderRequest.FulfillmentItem fulfillItem : request.getItems()) {
            OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().toString().equals(fulfillItem.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found: " + fulfillItem.getId()));
            
            if (fulfillItem.getQuantity() <= 0) {
                throw new RuntimeException("Invalid quantity for item: " + fulfillItem.getId());
            }
            
            // 这里可以添加更多的业务逻辑，比如库存检查等
        }
        
        // 更新订单配送状态
        order.setFulfillmentStatus(FulfillmentStatus.fulfilled);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 如果有元数据，更新订单元数据
        if (request.getMetadata() != null) {
            if (order.getMetadata() == null) {
                order.setMetadata(request.getMetadata());
            } else {
                order.getMetadata().putAll(request.getMetadata());
            }
        }
        
        // 创建订单变更记录
        createOrderChange(order, "Order fulfilled");
        
        // 保存订单
        order = orderRepository.save(order);
        
        // 返回更新后的订单信息
        return getOrder(id);
    }
} 