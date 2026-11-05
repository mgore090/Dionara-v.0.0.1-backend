package com.dianora.backend.returns.dto;

import com.dianora.backend.returns.entity.ReturnStatus;
import com.dianora.backend.returns.entity.RefundMethod;
import jakarta.validation.constraints.*;

public record AdminReturnUpdateRequest(
        @NotNull ReturnStatus status,
        RefundMethod refundMethod,
        @Size(max = 150) String refundDestination,
        @Size(max = 120) String refundTransactionId,
        @Size(max = 1000) String adminNote
) {}
