package com.dianora.backend.review.controller;

import com.dianora.backend.review.dto.CreateReviewRequest;
import com.dianora.backend.review.dto.ReviewResponse;
import com.dianora.backend.review.dto.UpdateReviewRequest;
import com.dianora.backend.review.entity.ReviewStatus;
import com.dianora.backend.review.service.ReviewService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Customer submits a review
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(request);
    }

    // Public product page: approved reviews only
    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> getProductReviews(
            @PathVariable Long productId) {
        return reviewService.getApprovedReviewsForProduct(productId);
    }

    // Reviews submitted by a particular user
    @GetMapping("/users/{userId}/reviews")
    public List<ReviewResponse> getUserReviews(
            @PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    // Customer edits their own pending review
    @PutMapping("/users/{userId}/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(
                userId, reviewId, request
        );
    }

    // Admin moderation endpoint: must be admin-protected
    @GetMapping("/admin/reviews/pending")
    public List<ReviewResponse> getPendingReviews() {
        return reviewService.getPendingReviews();
    }

    // Admin approves or rejects a review
    @PatchMapping("/admin/reviews/{reviewId}/moderate")
    public ReviewResponse moderateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body) {

        String statusValue = body.get("status");

        if (statusValue == null) {
            throw new org.springframework.web.server
                    .ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status is required"
            );
        }

        ReviewStatus status;

        try {
            status = ReviewStatus.valueOf(
                    statusValue.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server
                    .ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid review status"
            );
        }

        return reviewService.moderateReview(reviewId, status);
    }
}