package com.qvtu.mallshopping.controller;

import com.qvtu.mallshopping.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @GetMapping
    public ResponseEntity<?> listPayments(
        @RequestParam(required = false) Integer offset,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            return ResponseEntity.ok(paymentService.listPayments(offset, limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/payment-providers")
    public ResponseEntity<?> listPaymentProviders(
        @RequestParam(required = false) Integer offset,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            return ResponseEntity.ok(paymentService.listPaymentProviders(offset, limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment", paymentService.getPayment(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/capture")
    public ResponseEntity<?> capturePayment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment", paymentService.capturePayment(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "payment", paymentService.refundPayment(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", e.getMessage()));
        }
    }
} 