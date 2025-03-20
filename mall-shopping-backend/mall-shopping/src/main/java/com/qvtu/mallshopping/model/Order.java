package com.qvtu.mallshopping.model;

import com.qvtu.mallshopping.enums.PaymentStatus;
import com.qvtu.mallshopping.enums.FulfillmentStatus;
import com.qvtu.mallshopping.config.JpaJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.qvtu.mallshopping.enums.OrderStatus;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer version;
    
    @Column(name = "region_id")
    private String regionId;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "sales_channel_id")
    private String salesChannelId;
    
    private String email;
    
    @Column(name = "currency_code")
    private String currencyCode;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Column(name = "fulfillment_status")
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<ShippingMethod> shippingMethods;
    
    @Column(name = "item_total")
    private BigDecimal itemTotal;
    
    private BigDecimal subtotal;
    
    @Column(name = "tax_total")
    private BigDecimal taxTotal;
    
    @Column(name = "shipping_total")
    private BigDecimal shippingTotal;
    
    @Column(name = "discount_total")
    private BigDecimal discountTotal;
    
    private BigDecimal total;
    
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "display_id")
    private Integer displayId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<PaymentCollection> paymentCollections;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
    
    @Embedded
    private OrderSummary summary;
    
    @Column(name = "gift_card_total")
    private BigDecimal giftCardTotal;
    
    @Column(name = "gift_card_tax_total")
    private BigDecimal giftCardTaxTotal;
    
    @Column(name = "original_shipping_total")
    private BigDecimal originalShippingTotal;
    
    @Column(name = "original_shipping_subtotal")
    private BigDecimal originalShippingSubtotal;
    
    @Column(name = "original_shipping_tax_total")
    private BigDecimal originalShippingTaxTotal;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 