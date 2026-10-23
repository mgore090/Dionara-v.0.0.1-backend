package com.dianora.backend.coupon.controller;

import com.dianora.backend.coupon.dto.CreateCouponRequest;
import com.dianora.backend.coupon.dto.CouponResponse;
import com.dianora.backend.coupon.dto.UpdateCouponRequest;
import com.dianora.backend.coupon.dto.ValidateCouponRequest;
import com.dianora.backend.coupon.service.CouponService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // Admin: create coupon
    @PostMapping("/admin/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {
        return couponService.createCoupon(request);
    }

    // Get coupon details by code
    @GetMapping("/coupons/{code}")
    public CouponResponse getCoupon(@PathVariable String code) {
        return couponService.getCouponByCode(code);
    }

    // Admin: activate/deactivate coupon
    @PatchMapping("/admin/coupons/{couponId}")
    public CouponResponse updateCouponStatus(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {
        return couponService.updateCouponStatus(couponId, request);
    }

    // Validate coupon against an existing user's order
    @PostMapping("/coupons/validate")
    public Map<String, Object> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {

        BigDecimal discount =
                couponService.calculateDiscountForOrder(
                        request.orderId(),
                        request.userId(),
                        request.code()
                );

        return Map.of(
                "valid", true,
                "code", request.code().trim().toUpperCase(),
                "discountAmount", discount
        );
    }
}