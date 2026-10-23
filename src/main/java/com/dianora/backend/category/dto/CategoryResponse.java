package com.dianora.backend.category.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active,
        long productCount
) {
}
