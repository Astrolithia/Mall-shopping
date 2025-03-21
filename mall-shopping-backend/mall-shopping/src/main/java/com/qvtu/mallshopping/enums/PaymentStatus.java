package com.qvtu.mallshopping.enums;

public enum PaymentStatus {
    PENDING("pending"),
    PAID("paid"),            // 添加 paid 状态
    CAPTURED("captured"),
    REFUNDED("refunded"),
    CANCELLED("cancelled"),
    REQUIRES_ACTION("requires_action");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus value: " + value);
    }
} 