package com.dianora.backend.order.dto;

import com.dianora.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        OrderStatus status,

        // ADDED FOR COUPON:
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        String couponCode,

        BigDecimal totalAmount,

        String shippingFullName,
        String shippingPhone,
        String shippingAddressLine1,
        String shippingAddressLine2,
        String shippingCity,
        String shippingState,
        String shippingCountry,
        String shippingPostalCode,

        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}