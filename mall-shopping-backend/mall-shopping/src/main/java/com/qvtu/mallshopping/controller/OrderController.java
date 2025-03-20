package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CreateDraftOrderRequest;
import com.qvtu.mallshopping.dto.UpdateOrderRequest;
import com.qvtu.mallshopping.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> listOrders(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Map<String, Object> response = orderService.listOrders(offset, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/draft")
    public ResponseEntity<?> createDraftOrder(@RequestBody CreateDraftOrderRequest request) {
        try {
            return ResponseEntity.ok(Map.of(
                "draft_order", orderService.createDraftOrder(request)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<?> getDraftOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "draft_order", orderService.getDraftOrder(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "order", orderService.getOrder(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> updateOrder(
        @PathVariable Long id,
        @RequestBody(required = false) UpdateOrderRequest request
    ) {
        try {
            return ResponseEntity.ok(Map.of(
                "order", orderService.updateOrder(id, request)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }
} 