package com.dianora.backend.review.dto;

import com.dianora.backend.review.entity.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long userId,
        Long productId,
        Integer rating,
        String title,
        String comment,
        ReviewStatus status,
        boolean verifiedPurchase,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}