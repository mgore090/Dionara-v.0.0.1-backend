package com.dianora.backend.review.service;

import com.dianora.backend.product.entity.Product;
import com.dianora.backend.product.repository.ProductRepository;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import com.dianora.backend.review.dto.CreateReviewRequest;
import com.dianora.backend.review.dto.ReviewResponse;
import com.dianora.backend.review.dto.UpdateReviewRequest;
import com.dianora.backend.review.entity.Review;
import com.dianora.backend.review.entity.ReviewStatus;
import com.dianora.backend.review.repository.ReviewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot review an inactive product"
            );
        }

        if (reviewRepository.existsByUser_IdAndProduct_Id(
                user.getId(), product.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already reviewed this product"
            );
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());

        // New reviews require moderation.
        review.setStatus(ReviewStatus.PENDING);

        // Set to true only after verifying a completed purchase
        // against the user's order history.
        review.setVerifiedPurchase(false);

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getApprovedReviewsForProduct(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found"
            );
        }

        return reviewRepository
                .findAllByProduct_IdAndStatusOrderByCreatedAtDesc(
                        productId, ReviewStatus.APPROVED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            );
        }

        return reviewRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse updateReview(
            Long userId,
            Long reviewId,
            UpdateReviewRequest request) {

        Review review = reviewRepository
                .findByIdAndUser_Id(reviewId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Review not found for this user"
                ));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending reviews can be edited"
            );
        }

        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getPendingReviews() {
        return reviewRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        ReviewStatus.PENDING
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse moderateReview(
            Long reviewId,
            ReviewStatus newStatus) {

        if (newStatus != ReviewStatus.APPROVED
                && newStatus != ReviewStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be APPROVED or REJECTED"
            );
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"
                ));

        review.setStatus(newStatus);

        return toResponse(reviewRepository.save(review));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getProduct().getId(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus(),
                review.isVerifiedPurchase(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}