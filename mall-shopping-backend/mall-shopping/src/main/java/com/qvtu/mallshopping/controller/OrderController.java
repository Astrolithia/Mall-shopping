package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CreateDraftOrderRequest;
import com.qvtu.mallshopping.dto.UpdateOrderRequest;
import com.qvtu.mallshopping.dto.ArchiveOrderRequest;
import com.qvtu.mallshopping.dto.CompleteOrderRequest;
import com.qvtu.mallshopping.dto.FulfillOrderRequest;
import com.qvtu.mallshopping.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
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
            log.debug("Received list orders request with offset: {} and limit: {}", offset, limit);
            Map<String, Object> response = orderService.listOrders(offset, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing orders: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "message", "Error listing orders: " + e.getMessage(),
                    "details", e.toString()
                ));
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
            log.debug("Received get order request for ID: {}", id);
            Map<String, Object> order = orderService.getOrder(id);
            return ResponseEntity.ok(Map.of("order", order));
        } catch (Exception e) {
            log.error("Error getting order: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "message", "Error getting order: " + e.getMessage(),
                    "details", e.toString()
                ));
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> updateOrder(
        @PathVariable Long id,
        @RequestBody UpdateOrderRequest request
    ) {
        try {
            log.info("Received update order request for ID: {}", id);
            log.info("Request body: {}", request);
            
            Map<String, Object> result = orderService.updateOrder(id, request);
            
            log.info("Order updated successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to update order: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<?> archiveOrder(
        @PathVariable Long id,
        @RequestBody ArchiveOrderRequest request
    ) {
        try {
            return ResponseEntity.ok(Map.of(
                "order", orderService.archiveOrder(id, request)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "order", orderService.cancelOrder(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/changes")
    public ResponseEntity<?> getOrderChanges(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "order_changes", orderService.getOrderChanges(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "order", orderService.completeOrder(id, new CompleteOrderRequest())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/fulfillments")
    public ResponseEntity<?> fulfillOrder(
        @PathVariable Long id,
        @RequestBody FulfillOrderRequest request
    ) {
        log.debug("=== Starting fulfillOrder ===");
        log.debug("Request path variable id: {}", id);
        log.debug("Request body: {}", request);
        
        try {
            Map<String, Object> result = orderService.fulfillOrder(id, request);
            log.debug("Order fulfilled successfully");
            return ResponseEntity.ok(Map.of("order", result));
        } catch (Exception e) {
            log.error("Failed to fulfill order", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        } finally {
            log.debug("=== Ending fulfillOrder ===");
        }
    }
} 