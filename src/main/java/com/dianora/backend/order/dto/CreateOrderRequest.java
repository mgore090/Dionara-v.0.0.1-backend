package com.dianora.backend.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Address ID is required")
        Long addressId,

        @NotEmpty(message = "Order must contain at least one product")
        List<OrderItemRequest> items,

        // ADDED FOR COUPON:
        // Optional; null or blank means no coupon.
        @Size(max = 50)
        String couponCode

) {
}