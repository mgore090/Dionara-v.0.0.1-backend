package com.dianora.backend.coupon.repository;

import com.dianora.backend.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponUsageRepository
        extends JpaRepository<CouponUsage, Long> {

    long countByCoupon_IdAndUser_IdAndReleasedFalse(
            Long couponId,
            Long userId
    );

    Optional<CouponUsage> findByOrder_Id(Long orderId);

    boolean existsByOrder_Id(Long orderId);
}