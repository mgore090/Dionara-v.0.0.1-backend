package com.dianora.backend.coupon.service;

import com.dianora.backend.coupon.dto.CreateCouponRequest;
import com.dianora.backend.coupon.dto.CouponResponse;
import com.dianora.backend.coupon.dto.UpdateCouponRequest;
import com.dianora.backend.coupon.entity.Coupon;
import com.dianora.backend.coupon.entity.CouponType;
import com.dianora.backend.coupon.entity.CouponUsage;
import com.dianora.backend.coupon.repository.CouponRepository;
import com.dianora.backend.coupon.repository.CouponUsageRepository;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.repository.OrderRepository;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public CouponService(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        String code = normalizeCode(request.code());

        if (couponRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Coupon code already exists"
            );
        }

        if (request.startsAt().isAfter(request.expiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon start date must be before expiry date"
            );
        }

        if (request.minimumOrderAmount() != null
                && request.minimumOrderAmount().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum order amount cannot be negative"
            );
        }

        if (request.type() == CouponType.PERCENTAGE
                && request.discountValue().compareTo(
                new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100"
            );
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setName(request.name().trim());
        coupon.setDescription(request.description());
        coupon.setType(request.type());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(
                request.minimumOrderAmount() == null
                        ? BigDecimal.ZERO
                        : request.minimumOrderAmount()
        );
        coupon.setMaximumDiscountAmount(
                request.maximumDiscountAmount()
        );
        coupon.setUsageLimit(request.usageLimit());
        coupon.setPerUserLimit(request.perUserLimit());
        coupon.setUsedCount(0);
        coupon.setStartsAt(request.startsAt());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setActive(true);

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String rawCode) {
        String code = normalizeCode(rawCode);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coupon not found"
                ));

        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse updateCouponStatus(
            Long couponId,
            UpdateCouponRequest request) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coupon not found"
                ));

        coupon.setActive(request.active());

        return toResponse(couponRepository.save(coupon));
    }

    /*
     * Called by the order creation transaction.
     * It validates the coupon, calculates the discount,
     * and reserves one use by incrementing usedCount.
     */
    @Transactional
    public void applyCouponToOrder(
            Order order,
            String rawCode,
            BigDecimal subtotal) {

        String code = normalizeCode(rawCode);

        Coupon coupon = couponRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid coupon code"
                ));

        validateCoupon(coupon, order.getUser().getId(), subtotal);

        BigDecimal discount = calculateDiscount(coupon, subtotal);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        order.setCoupon(coupon);
        order.setSubtotalAmount(money(subtotal));
        order.setDiscountAmount(discount);
        order.setTotalAmount(money(subtotal.subtract(discount)));
    }

    /*
     * Call only after the order has been saved and has an ID.
     * Must run inside the same transaction as order creation.
     */
    @Transactional
    public void recordUsage(Order order) {
        if (order.getCoupon() == null) {
            return;
        }

        if (couponUsageRepository.existsByOrder_Id(order.getId())) {
            return;
        }

        CouponUsage usage = new CouponUsage();
        usage.setCoupon(order.getCoupon());
        usage.setUser(order.getUser());
        usage.setOrder(order);
        usage.setReleased(false);

        couponUsageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscountForOrder(
            Long orderId,
            Long userId,
            String rawCode) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This order does not belong to the user"
            );
        }

        if (order.getCoupon() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A coupon has already been applied to this order"
            );
        }

        String code = normalizeCode(rawCode);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid coupon code"
                ));

        validateCoupon(
                coupon, userId, order.getSubtotalAmount()
        );

        return calculateDiscount(
                coupon, order.getSubtotalAmount()
        );
    }

    /*
     * Call this inside the cancellation transaction,
     * after verifying the order is eligible for cancellation.
     * It is safe to call more than once for the same order.
     */
    @Transactional
    public void releaseUsageForCancelledOrder(Long orderId) {
        CouponUsage usage = couponUsageRepository
                .findByOrder_Id(orderId)
                .orElse(null);

        if (usage == null || usage.isReleased()) {
            return;
        }

        Coupon coupon = couponRepository
                .findById(usage.getCoupon().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coupon not found"
                ));

        coupon.setUsedCount(
                Math.max(0, coupon.getUsedCount() - 1)
        );
        couponRepository.save(coupon);

        usage.setReleased(true);
        usage.setReleasedAt(LocalDateTime.now());
        couponUsageRepository.save(usage);
    }

    private void validateCoupon(
            Coupon coupon,
            Long userId,
            BigDecimal subtotal) {

        LocalDateTime now = LocalDateTime.now();

        if (!coupon.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Coupon is inactive"
            );
        }

        if (now.isBefore(coupon.getStartsAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Coupon is not active yet"
            );
        }

        if (now.isAfter(coupon.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Coupon has expired"
            );
        }

        if (coupon.getUsageLimit() > 0
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Coupon usage limit has been reached"
            );
        }

        if (subtotal == null || subtotal.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order subtotal must be greater than zero"
            );
        }

        if (coupon.getMinimumOrderAmount() != null
                && subtotal.compareTo(
                coupon.getMinimumOrderAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order does not meet the minimum amount for this coupon"
            );
        }

        long userUsageCount =
                couponUsageRepository
                        .countByCoupon_IdAndUser_IdAndReleasedFalse(
                                coupon.getId(), userId
                        );

        if (userUsageCount >= coupon.getPerUserLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have reached this coupon's usage limit"
            );
        }
    }

    private BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal subtotal) {

        BigDecimal discount;

        if (coupon.getType() == CouponType.PERCENTAGE) {
            discount = subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2,
                            RoundingMode.HALF_UP);

            if (coupon.getMaximumDiscountAmount() != null) {
                discount = discount.min(
                        coupon.getMaximumDiscountAmount()
                );
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        // Discount can never exceed the subtotal.
        discount = discount.min(subtotal);

        return money(discount);
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon code is required"
            );
        }

        return rawCode.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.getMaximumDiscountAmount(),
                coupon.getUsageLimit(),
                coupon.getPerUserLimit(),
                coupon.getUsedCount(),
                coupon.getStartsAt(),
                coupon.getExpiresAt(),
                coupon.isActive()
        );
    }
}