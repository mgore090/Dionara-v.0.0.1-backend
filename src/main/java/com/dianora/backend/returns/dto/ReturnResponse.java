package com.dianora.backend.returns.dto;

import com.dianora.backend.returns.entity.ReturnStatus;
import com.dianora.backend.returns.entity.RefundMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReturnResponse(
        Long id,
        String returnNumber,
        Long orderId,
        String orderNumber,
        Long orderItemId,
        String productName,
        Integer orderedQuantity,
        Integer returnQuantity,
        String reason,
        String additionalDetails,
        ReturnStatus status,
        BigDecimal refundAmount,
        RefundMethod refundMethod,
        String refundDestination,
        String refundTransactionId,
        String adminNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime refundedAt
) {}
