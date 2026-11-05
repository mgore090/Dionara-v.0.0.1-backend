package com.dianora.backend.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(

        @NotBlank(message = "Operation is required")
        String operation,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        Integer quantity,

        @NotBlank(message = "Reason is required")
        String reason

) {
}

