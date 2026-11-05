
package com.dianora.backend.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        long totalOrders,
        long totalReviews,
        BigDecimal totalSpent,
        List<CustomerOrderResponse> orders,
        List<CustomerAddressResponse> addresses,
        List<CustomerReviewResponse> reviews
) {

    public record CustomerOrderResponse(
            Long id,
            String orderNumber,
            String status,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            List<CustomerOrderItemResponse> items
    ) {}

    public record CustomerOrderItemResponse(
            Long id,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {}

    public record CustomerAddressResponse(
            Long id,
            String fullName,
            String phone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String country,
            String postalCode,
            String addressType,
            boolean defaultAddress
    ) {}

    public record CustomerReviewResponse(
            Long id,
            Long productId,
            String productName,
            Integer rating,
            String title,
            String comment,
            String status,
            boolean verifiedPurchase,
            LocalDateTime createdAt
    ) {}
}
