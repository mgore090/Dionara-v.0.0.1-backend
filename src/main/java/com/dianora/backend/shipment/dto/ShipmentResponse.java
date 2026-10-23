package com.dianora.backend.shipment.dto;

import com.dianora.backend.shipment.entity.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        Long orderId,
        String orderNumber,
        String courierName,
        String trackingNumber,
        String trackingUrl,
        ShipmentStatus status,
        LocalDateTime shippedAt,
        LocalDateTime estimatedDeliveryAt,
        LocalDateTime deliveredAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}