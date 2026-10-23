package com.dianora.backend.payment.service;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.entity.OrderStatus;
import com.dianora.backend.order.repository.OrderRepository;
import com.dianora.backend.payment.dto.CreatePaymentRequest;
import com.dianora.backend.payment.dto.PaymentResponse;
import com.dianora.backend.payment.entity.Payment;
import com.dianora.backend.payment.entity.PaymentMethod;
import com.dianora.backend.payment.entity.PaymentStatus;
import com.dianora.backend.payment.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // Create a pending payment attempt for an existing order.
    public PaymentResponse createPayment(
            Long orderId,
            CreatePaymentRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot pay for a cancelled order"
            );
        }

        if (order.getTotalAmount() == null
                || order.getTotalAmount().signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order amount must be greater than zero"
            );
        }

        if (paymentRepository.existsByOrder_IdAndStatus(
                orderId, PaymentStatus.SUCCESS)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order has already been paid"
            );
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("INR");
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.PENDING);

        // Gateway integration will populate these fields later.
        if (request.method() != PaymentMethod.COD) {
            payment.setGateway("NOT_CONFIGURED");
        } else {
            payment.setGateway("COD");
        }

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found: " + paymentId
                ));

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getOrderPayments(Long orderId) {

        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found: " + orderId
            );
        }

        return paymentRepository
                .findAllByOrder_IdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * This method is intended to be called only after the payment
     * gateway's signature/webhook has been verified server-side.
     * Do not expose it as an unauthenticated public endpoint.
     */
    public PaymentResponse markPaymentSuccessfulAfterVerification(
            Long paymentId,
            String gatewayPaymentId,
            String gatewayOrderId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found: " + paymentId
                ));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return mapToResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending payments can be marked successful"
            );
        }

        if (gatewayPaymentId == null || gatewayPaymentId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verified gateway payment ID is required"
            );
        }

        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        return mapToResponse(paymentRepository.save(payment));
    }

    /*
     * This method must only be called after a trusted gateway
     * failure notification has been verified.
     */
    public PaymentResponse markPaymentFailedAfterVerification(
            Long paymentId,
            String failureReason) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found: " + paymentId
                ));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending payments can be marked failed"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(
                failureReason == null
                        ? "Payment failed"
                        : failureReason
        );

        return mapToResponse(paymentRepository.save(payment));
    }

    private PaymentResponse mapToResponse(Payment payment) {

        Order order = payment.getOrder();

        return new PaymentResponse(
                payment.getId(),
                order.getId(),
                order.getOrderNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getGateway(),
                payment.getGatewayPaymentId(),
                payment.getGatewayOrderId(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}