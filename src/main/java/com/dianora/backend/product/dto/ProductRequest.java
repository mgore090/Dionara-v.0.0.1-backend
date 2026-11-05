package com.dianora.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150)
        String name,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Size(max = 3000)
        String description,

        @Size(max = 1000)
        String imageUrl,

        @NotNull(message = "Category is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @Min(value = 0, message = "Stock cannot be negative")
        int stock

) {
}
