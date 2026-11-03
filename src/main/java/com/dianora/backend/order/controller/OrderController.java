package com.dianora.backend.order.controller;

import com.dianora.backend.order.dto.CreateOrderRequest;
import com.dianora.backend.order.dto.OrderResponse;
import com.dianora.backend.order.dto.UpdateOrderStatusRequest;
import com.dianora.backend.order.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Customer creates an order.
    // ADDED FOR COUPON: request may contain couponCode.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // Get one order by ID.
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable("orderId") Long orderId) {
        return orderService.getOrder(orderId);
    }

    // Get a user's order history.
    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(
            @PathVariable("userId") Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    // Admin/authorized staff only.
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(
                orderId,
                request.status()
        );
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
}