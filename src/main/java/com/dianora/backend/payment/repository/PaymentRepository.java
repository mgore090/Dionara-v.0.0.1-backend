package com.dianora.backend.payment.repository;

import com.dianora.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrder_IdOrderByCreatedAtDesc(Long orderId);

    Optional<Payment> findByIdAndOrder_Id(
            Long paymentId,
            Long orderId
    );

    boolean existsByOrder_IdAndStatus(
            Long orderId,
            com.dianora.backend.payment.entity.PaymentStatus status
    );
}