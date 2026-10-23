package com.dianora.backend.payment.dto;

import com.dianora.backend.payment.entity.PaymentMethod;
import com.dianora.backend.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String orderNumber,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String gateway,
        String gatewayPaymentId,
        String gatewayOrderId,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}