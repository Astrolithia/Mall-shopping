package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.dto.CreatePaymentCollectionRequest;
import com.qvtu.mallshopping.dto.MarkAsPaidRequest;
import com.qvtu.mallshopping.service.PaymentCollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-collections")
public class PaymentCollectionController {
    private final PaymentCollectionService paymentCollectionService;
    
    public PaymentCollectionController(PaymentCollectionService paymentCollectionService) {
        this.paymentCollectionService = paymentCollectionService;
    }
    
    @PostMapping
    public ResponseEntity<?> createPaymentCollection(@RequestBody CreatePaymentCollectionRequest request) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment_collection", paymentCollectionService.createPaymentCollection(request)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaymentCollection(@PathVariable Long id) {
        try {
            paymentCollectionService.deletePaymentCollection(id);
            return ResponseEntity.ok(Map.of(
                "id", id.toString(),
                "object", "payment-collection",
                "deleted", true
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedPaymentCollections() {
        try {
            return ResponseEntity.ok(Map.of(
                "payment_collections", paymentCollectionService.seedPaymentCollections()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/random")
    public ResponseEntity<?> generateRandomPaymentCollection() {
        try {
            return ResponseEntity.ok(Map.of(
                "payment_collection", paymentCollectionService.generateRandomPaymentCollection()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/random/batch")
    public ResponseEntity<?> generateRandomPaymentCollections(
        @RequestParam(defaultValue = "5") int count
    ) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment_collections", paymentCollectionService.generateRandomPaymentCollections(count)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/mark-as-paid")
    public ResponseEntity<?> markAsPaid(
        @PathVariable Long id,
        @RequestBody MarkAsPaidRequest request
    ) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment_collection", paymentCollectionService.markAsPaid(id, request)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }
} 