package com.dianora.backend.returns.dto;

import jakarta.validation.constraints.*;

public record CreateReturnRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long orderId,
        @NotNull @Positive Long orderItemId,
        @NotNull @Positive Integer quantity,
        @NotBlank @Size(max = 80) String reason,
        @Size(max = 1500) String additionalDetails
) {}
