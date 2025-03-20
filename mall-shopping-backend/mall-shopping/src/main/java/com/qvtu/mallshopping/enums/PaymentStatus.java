package com.qvtu.mallshopping.enums;

public enum PaymentStatus {
    not_paid,        // 未支付
    awaiting,        // 等待支付
    captured,        // 已收款
    partially_paid,  // 部分支付
    paid,           // 已支付
    canceled,       // 已取消
    requires_action // 需要处理
} 