package com.dianora.backend.review.repository;

import com.dianora.backend.review.entity.Review;
import com.dianora.backend.review.entity.ReviewStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    Optional<Review> findByIdAndUser_Id(
            Long reviewId,
            Long userId
    );

    List<Review> findAllByProduct_IdAndStatusOrderByCreatedAtDesc(
            Long productId,
            ReviewStatus status
    );

    List<Review> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    List<Review> findAllByStatusOrderByCreatedAtDesc(
            ReviewStatus status
    );
}