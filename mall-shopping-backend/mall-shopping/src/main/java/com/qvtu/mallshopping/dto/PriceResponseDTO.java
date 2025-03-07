package com.qvtu.mallshopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceResponseDTO {
    private String currency;
    private BigDecimal amount;
    private Boolean taxInclusive;
}
