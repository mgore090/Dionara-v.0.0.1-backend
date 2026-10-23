package com.dianora.backend.coupon.dto;

import com.dianora.backend.coupon.entity.CouponType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        CouponType type,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00")
        BigDecimal minimumOrderAmount,

        @DecimalMin(value = "0.01")
        BigDecimal maximumDiscountAmount,

        @NotNull
        @Min(0)
        Integer usageLimit,

        @NotNull
        @Min(1)
        Integer perUserLimit,

        @NotNull
        LocalDateTime startsAt,

        @NotNull
        LocalDateTime expiresAt

) {}