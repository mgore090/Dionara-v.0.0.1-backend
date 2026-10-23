package com.dianora.backend.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}