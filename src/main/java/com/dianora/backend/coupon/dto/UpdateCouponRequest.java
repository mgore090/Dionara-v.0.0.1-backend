package com.dianora.backend.coupon.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCouponRequest(
        @NotNull Boolean active
) {}