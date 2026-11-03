package com.dianora.backend.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String description,
        String imageUrl,
        int stock,
        boolean active,
        Long categoryId,
        String categoryName,
        BigDecimal originalPrice,
        List<String> images,
        List<String> features,
        String size
) {
}