package com.dianora.backend.returns.service;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.entity.OrderItem;
import com.dianora.backend.order.entity.OrderStatus;
import com.dianora.backend.order.repository.OrderRepository;
import com.dianora.backend.returns.dto.AdminReturnUpdateRequest;
import com.dianora.backend.returns.dto.CreateReturnRequest;
import com.dianora.backend.returns.dto.ReturnResponse;
import com.dianora.backend.returns.entity.RefundMethod;
import com.dianora.backend.returns.entity.ReturnRequest;
import com.dianora.backend.returns.entity.ReturnStatus;
import com.dianora.backend.returns.repository.ReturnRequestRepository;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnService {

    private final ReturnRequestRepository returnRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReturnService(ReturnRequestRepository returnRepository,
                         OrderRepository orderRepository,
                         UserRepository userRepository) {
        this.returnRepository = returnRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReturnResponse createReturn(CreateReturnRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Order order = orderRepository.findByIdAndUser_Id(request.orderId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found for this customer"));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Return can be requested only after the order is delivered");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(request.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order item not found in this order"));

        if (request.quantity() > item.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Return quantity cannot exceed purchased quantity");
        }

        List<ReturnStatus> activeStatuses = List.of(
                ReturnStatus.REQUESTED, ReturnStatus.APPROVED,
                ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.RECEIVED,
                ReturnStatus.REFUND_PROCESSING, ReturnStatus.REFUNDED
        );

        int previouslyRequested = returnRepository
                .findAllByOrderItem_IdAndStatusIn(item.getId(), activeStatuses)
                .stream()
                .mapToInt(ReturnRequest::getQuantity)
                .sum();

        if (previouslyRequested + request.quantity() > item.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Return quantity already requested for this product");
        }

        ReturnRequest entity = new ReturnRequest();
        entity.setOrder(order);
        entity.setOrderItem(item);
        entity.setUser(user);
        entity.setQuantity(request.quantity());
        entity.setReason(request.reason().trim());
        entity.setAdditionalDetails(request.additionalDetails());
        entity.setStatus(ReturnStatus.REQUESTED);

        BigDecimal refund = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.quantity()))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        entity.setRefundAmount(refund);

        return toResponse(returnRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getMyReturns(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return returnRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsForOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found for this customer"));
        return returnRepository.findAllByOrder_IdOrderByCreatedAtDesc(order.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getAllReturnsForAdmin() {
        return returnRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReturnResponse updateReturnForAdmin(Long returnId, AdminReturnUpdateRequest request) {
        ReturnRequest entity = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Return request not found"));

        ReturnStatus current = entity.getStatus();
        ReturnStatus next = request.status();

        if (current == ReturnStatus.REJECTED || current == ReturnStatus.REFUNDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Rejected or refunded return request is terminal");
        }

        boolean allowed = switch (current) {
            case REQUESTED -> next == ReturnStatus.APPROVED || next == ReturnStatus.REJECTED;
            case APPROVED -> next == ReturnStatus.PICKUP_SCHEDULED || next == ReturnStatus.RECEIVED;
            case PICKUP_SCHEDULED -> next == ReturnStatus.RECEIVED;
            case RECEIVED -> next == ReturnStatus.REFUND_PROCESSING;
            case REFUND_PROCESSING -> next == ReturnStatus.REFUNDED;
            default -> false;
        };

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid return status transition: " + current + " -> " + next);
        }

        if (next == ReturnStatus.REFUND_PROCESSING || next == ReturnStatus.REFUNDED) {
            if (request.refundMethod() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Refund method is required for refund status");
            }
            entity.setRefundMethod(request.refundMethod());
            entity.setRefundDestination(request.refundDestination());
        }

        if (next == ReturnStatus.REFUNDED) {
            if (request.refundTransactionId() == null || request.refundTransactionId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Refund transaction ID is required when marking refund completed");
            }
            entity.setRefundTransactionId(request.refundTransactionId().trim());
            entity.setRefundedAt(LocalDateTime.now());
        }

        // Stock should be returned to inventory only after returned goods are received.
        if (next == ReturnStatus.RECEIVED && current != ReturnStatus.RECEIVED) {
            var product = entity.getOrderItem().getProduct();
            product.setStock(product.getStock() + entity.getQuantity());
        }

        entity.setStatus(next);
        entity.setAdminNote(request.adminNote());
        return toResponse(returnRepository.save(entity));
    }

    private ReturnResponse toResponse(ReturnRequest r) {
        return new ReturnResponse(
                r.getId(),
                r.getReturnNumber(),
                r.getOrder().getId(),
                r.getOrder().getOrderNumber(),
                r.getOrderItem().getId(),
                r.getOrderItem().getProductName(),
                r.getOrderItem().getQuantity(),
                r.getQuantity(),
                r.getReason(),
                r.getAdditionalDetails(),
                r.getStatus(),
                r.getRefundAmount(),
                r.getRefundMethod(),
                r.getRefundDestination(),
                r.getRefundTransactionId(),
                r.getAdminNote(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getRefundedAt()
        );
    }
}
