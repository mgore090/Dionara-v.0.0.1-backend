package com.dianora.backend.payment.dto;

import com.dianora.backend.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod method

) {
}