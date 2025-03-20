package com.qvtu.mallshopping.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.qvtu.mallshopping.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusConverter.class);
    
    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return status.name().toLowerCase();
    }
    
    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(dbData.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.error("Error converting database value: {} to OrderStatus", dbData);
            log.error("Available values: {}", java.util.Arrays.toString(OrderStatus.values()));
            throw e;
        }
    }
} 