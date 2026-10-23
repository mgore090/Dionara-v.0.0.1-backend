package com.dianora.backend.order.service;

import com.dianora.backend.address.entity.Address;
import com.dianora.backend.address.repository.AddressRepository;

import com.dianora.backend.coupon.service.CouponService;

import com.dianora.backend.order.dto.CreateOrderRequest;
import com.dianora.backend.order.dto.OrderItemRequest;
import com.dianora.backend.order.dto.OrderItemResponse;
import com.dianora.backend.order.dto.OrderResponse;
import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.entity.OrderItem;
import com.dianora.backend.order.entity.OrderStatus;
import com.dianora.backend.order.repository.OrderRepository;

import com.dianora.backend.product.entity.Product;
import com.dianora.backend.product.repository.ProductRepository;

import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // ADDED FOR COUPON:
    private final CouponService couponService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,

            // ADDED FOR COUPON:
            CouponService couponService) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;

        // ADDED FOR COUPON:
        this.couponService = couponService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1. Find and validate user.
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User account is inactive"
            );
        }

        // 2. Validate that the shipping address belongs to this user.
        Address address = addressRepository
                .findByIdAndUser_Id(request.addressId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Shipping address not found for this user"
                ));

        // 3. Merge duplicate product IDs in the request.
        // Example: product 10 quantity 1 + product 10 quantity 2
        // becomes product 10 quantity 3.
        Map<Long, Integer> quantities = new HashMap<>();

        for (OrderItemRequest itemRequest : request.items()) {
            if (itemRequest.quantity() == null
                    || itemRequest.quantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product quantity must be greater than zero"
                );
            }

            quantities.merge(
                    itemRequest.productId(),
                    itemRequest.quantity(),
                    Math::addExact
            );
        }

        // 4. Create the order and snapshot the shipping address.
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);

        order.setShippingFullName(address.getFullName());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingCountry(address.getCountry());
        order.setShippingPostalCode(address.getPostalCode());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();

        // 5. Lock products, validate availability and reserve stock.
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {

            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productRepository
                    .findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + productId
                    ));

            if (!product.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product is inactive: " + product.getName()
                );
            }

            if (product.getStock() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice()
                    .setScale(2, RoundingMode.HALF_UP);

            if (unitPrice.signum() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid product price: " + product.getName()
                );
            }

            BigDecimal itemSubtotal = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(itemSubtotal);

            order.addItem(orderItem);

            subtotal = subtotal.add(itemSubtotal);
            lockedProducts.add(product);

            // Stock is changed in the current transaction.
            // If any later step fails, transaction rollback restores it.
            product.setStock(product.getStock() - quantity);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // 6. Default amounts for orders without a coupon.
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);

        // ADDED FOR COUPON:
        // Coupon is validated on the server and discount is calculated
        // from the server-side product subtotal, not client input.
        if (request.couponCode() != null
                && !request.couponCode().isBlank()) {

            couponService.applyCouponToOrder(
                    order,
                    request.couponCode(),
                    subtotal
            );
        }

        // 7. Persist order and order items.
        // CascadeType.ALL saves the attached OrderItem entities.
        Order savedOrder = orderRepository.saveAndFlush(order);

        // ADDED FOR COUPON:
        // Record coupon usage after the order has an ID.
        // This must remain in the same transaction as order creation.
        couponService.recordUsage(savedOrder);

        // 8. Return response with subtotal, discount and final amount.
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            );
        }

        return orderRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));

        OrderStatus current = order.getStatus();

        if (current == newStatus) {
            return toResponse(order);
        }

        // Prevent changing a completed or cancelled order.
        if (current == OrderStatus.DELIVERED
                || current == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Delivered or cancelled order cannot be updated"
            );
        }

        // Validate allowed transitions.
        boolean allowed = switch (current) {
            case PLACED ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.PROCESSING
                            || newStatus == OrderStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            default -> false;
        };

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status transition: "
                            + current + " -> " + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {

            // ADDED FOR COUPON:
            // Release coupon usage when cancellation is permitted.
            couponService.releaseUsageForCancelledOrder(order.getId());

            // Restore stock only for cancellation before shipment.
            for (OrderItem item : order.getItems()) {
                Product product = productRepository
                        .findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found while restoring stock"
                        ));

                product.setStock(
                        product.getStock() + item.getQuantity()
                );
            }
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getSubtotal()
                        ))
                        .toList();

        // ADDED FOR COUPON:
        // Return the applied coupon code, not the full coupon entity.
        String couponCode = order.getCoupon() != null
                ? order.getCoupon().getCode()
                : null;

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getStatus(),

                // ADDED FOR COUPON:
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                couponCode,

                order.getTotalAmount(),

                order.getShippingFullName(),
                order.getShippingPhone(),
                order.getShippingAddressLine1(),
                order.getShippingAddressLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingCountry(),
                order.getShippingPostalCode(),

                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}