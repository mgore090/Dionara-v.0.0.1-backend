package com.dianora.backend.coupon.dto;

import com.dianora.backend.coupon.entity.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String name,
        String description,
        CouponType type,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maximumDiscountAmount,
        Integer usageLimit,
        Integer perUserLimit,
        Integer usedCount,
        LocalDateTime startsAt,
        LocalDateTime expiresAt,
        boolean active
) {}