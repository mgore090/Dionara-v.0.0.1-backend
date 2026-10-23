package com.dianora.backend.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String description,
        String imageUrl,
        int stock,
        boolean active,
        Long categoryId,
        String categoryName
) {
}