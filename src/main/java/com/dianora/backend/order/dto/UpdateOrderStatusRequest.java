package com.dianora.backend.order.dto;

import com.dianora.backend.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Order status is required")
        OrderStatus status

) {
}