package com.qvtu.mallshopping.dto;

import com.qvtu.mallshopping.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class OrderDTO {
    private String id;
    private Integer version;
    private String regionId;
    private String customerId;
    private String salesChannelId;
    private String email;
    private String currencyCode;
    private String paymentStatus;
    private String fulfillmentStatus;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private List<ShippingMethodDTO> shippingMethods;
    private OrderSummaryDTO summary;
    private BigDecimal itemTotal;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingTotal;
    private BigDecimal discountTotal;
    private BigDecimal total;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaymentCollectionDTO> paymentCollections;

    public List<PaymentCollectionDTO> getPaymentCollections() {
        return paymentCollections;
    }

    public void setPaymentCollections(List<PaymentCollectionDTO> paymentCollections) {
        this.paymentCollections = paymentCollections;
    }
} 