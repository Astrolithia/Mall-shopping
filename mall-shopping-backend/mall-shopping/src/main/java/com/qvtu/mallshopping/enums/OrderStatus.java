package com.qvtu.mallshopping.enums;

public enum OrderStatus {
    PENDING("pending"),
    DRAFT("draft"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    ARCHIVED("archived"),
    REQUIRES_ACTION("requires_action");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus value: " + value);
    }
} 