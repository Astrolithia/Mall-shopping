package com.qvtu.mallshopping.enums;

public enum PaymentStatus {
    canceled,            // 已取消
    not_paid,           // 未支付
    awaiting,           // 等待支付
    authorized,         // 已授权
    partially_authorized, // 部分授权
    captured,           // 已收款
    partially_captured,  // 部分收款
    partially_refunded,  // 部分退款
    refunded,           // 已退款
    requires_action     // 需要处理
} 