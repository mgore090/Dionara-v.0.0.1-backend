package com.dianora.backend.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ValidateCouponRequest(

        @NotBlank
        String code,

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long orderId

) {}