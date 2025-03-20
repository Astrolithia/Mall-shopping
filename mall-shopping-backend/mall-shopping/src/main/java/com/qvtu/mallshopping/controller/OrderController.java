package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CreateDraftOrderRequest;
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
    public ResponseEntity<?> listOrders(
        @RequestParam(required = false) Integer offset,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            return ResponseEntity.ok(orderService.listOrders(offset, limit));
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
} 