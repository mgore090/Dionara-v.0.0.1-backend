
package com.dianora.backend.customer.service;

import com.dianora.backend.customer.dto.CustomerResponse;
import com.dianora.backend.customer.dto.CustomerResponse.CustomerAddressResponse;
import com.dianora.backend.customer.dto.CustomerResponse.CustomerOrderResponse;
import com.dianora.backend.customer.dto.CustomerResponse.CustomerOrderItemResponse;
import com.dianora.backend.customer.dto.CustomerResponse.CustomerReviewResponse;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.entity.OrderItem;
import com.dianora.backend.order.repository.OrderRepository;

import com.dianora.backend.review.entity.Review;
import com.dianora.backend.review.repository.ReviewRepository;

import com.dianora.backend.address.entity.Address;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public CustomerService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            ReviewRepository reviewRepository) {

        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {

        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found: " + customerId
                ));

        return toResponse(user);
    }

    private CustomerResponse toResponse(User user) {

        Long userId = user.getId();

        // Orders are linked through ORDERS.USER_ID.
        // ORDER_ITEMS does not need a USER_ID column.
        List<Order> orders =
                orderRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);

        List<CustomerOrderResponse> orderResponses = orders.stream()
                .map(order -> new CustomerOrderResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus() == null
                                ? "UNKNOWN"
                                : order.getStatus().name(),
                        order.getSubtotalAmount(),
                        order.getDiscountAmount(),
                        order.getTotalAmount(),
                        order.getCreatedAt(),
                        order.getItems() == null
                                ? List.of()
                                : order.getItems().stream()
                                .map(this::toOrderItemResponse)
                                .toList()
                ))
                .toList();

        // Saved customer addresses.
        List<CustomerAddressResponse> addressResponses =
                user.getAddresses() == null
                        ? List.of()
                        : user.getAddresses().stream()
                        .map(this::toAddressResponse)
                        .toList();

        // Customer reviews.
        List<Review> reviews =
                reviewRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);

        List<CustomerReviewResponse> reviewResponses = reviews.stream()
                .map(this::toReviewResponse)
                .toList();

        // Exclude cancelled and refunded orders from spending.
        BigDecimal totalSpent = orders.stream()
                .filter(order -> {
                    if (order.getStatus() == null) {
                        return false;
                    }

                    String status = order.getStatus()
                            .name()
                            .toUpperCase(Locale.ROOT);

                    return !status.equals("CANCELLED")
                            && !status.equals("REFUNDED");
                })
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getCreatedAt(),
                orderResponses.size(),
                reviewResponses.size(),
                totalSpent,
                orderResponses,
                addressResponses,
                reviewResponses
        );
    }

    private CustomerOrderItemResponse toOrderItemResponse(OrderItem item) {

        return new CustomerOrderItemResponse(
                item.getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    private CustomerAddressResponse toAddressResponse(Address address) {

        return new CustomerAddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getAddressType(),
                address.isDefaultAddress()
        );
    }

    private CustomerReviewResponse toReviewResponse(Review review) {

        return new CustomerReviewResponse(
                review.getId(),
                review.getProduct() == null
                        ? null
                        : review.getProduct().getId(),
                review.getProduct() == null
                        ? null
                        : review.getProduct().getName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus() == null
                        ? "UNKNOWN"
                        : review.getStatus().name(),
                review.isVerifiedPurchase(),
                review.getCreatedAt()
        );
    }
}
