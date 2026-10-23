package com.dianora.backend.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        Long userId,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
}