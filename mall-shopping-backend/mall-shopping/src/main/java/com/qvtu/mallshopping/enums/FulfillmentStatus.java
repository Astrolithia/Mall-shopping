package com.qvtu.mallshopping.enums;

public enum FulfillmentStatus {
    not_fulfilled,    // 未发货
    partially_fulfilled, // 部分发货
    fulfilled,        // 已发货
    shipped,         // 已配送
    canceled,        // 已取消
    requires_action  // 需要处理
} 