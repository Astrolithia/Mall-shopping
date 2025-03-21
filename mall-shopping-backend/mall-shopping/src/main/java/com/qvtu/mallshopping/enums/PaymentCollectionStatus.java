package com.qvtu.mallshopping.enums;

public enum PaymentCollectionStatus {
    NOT_PAID("not_paid"),
    AWAITING("awaiting"),
    AUTHORIZED("authorized"),
    PARTIALLY_AUTHORIZED("partially_authorized"),
    PAID("paid"),
    PARTIALLY_PAID("partially_paid"),
    REFUNDED("refunded"),
    PARTIALLY_REFUNDED("partially_refunded"),
    CANCELLED("cancelled"),
    REQUIRES_ACTION("requires_action");

    private final String value;

    PaymentCollectionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentCollectionStatus fromValue(String value) {
        for (PaymentCollectionStatus status : PaymentCollectionStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentCollectionStatus value: " + value);
    }
} 