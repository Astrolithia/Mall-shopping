package com.qvtu.mallshopping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArchiveOrderRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
} 