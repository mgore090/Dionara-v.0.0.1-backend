package com.dianora.backend.payment.controller;

import com.dianora.backend.payment.dto.CreatePaymentRequest;
import com.dianora.backend.payment.dto.PaymentResponse;
import com.dianora.backend.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Start a payment attempt for an order.
    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(orderId, request));
    }

    // Get a payment by its ID.
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPayment(paymentId));
    }

    // Get payment attempts for an order.
    @GetMapping("/orders/{orderId}/payments")
    public ResponseEntity<List<PaymentResponse>> getOrderPayments(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getOrderPayments(orderId));
    }
}