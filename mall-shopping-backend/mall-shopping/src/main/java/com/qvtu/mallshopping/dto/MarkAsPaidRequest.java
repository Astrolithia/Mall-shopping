package com.qvtu.mallshopping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkAsPaidRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
} 