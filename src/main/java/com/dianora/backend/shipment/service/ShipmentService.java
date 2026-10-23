package com.dianora.backend.shipment.service;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.repository.OrderRepository;
import com.dianora.backend.shipment.dto.CreateShipmentRequest;
import com.dianora.backend.shipment.dto.ShipmentResponse;
import com.dianora.backend.shipment.dto.UpdateShipmentRequest;
import com.dianora.backend.shipment.entity.Shipment;
import com.dianora.backend.shipment.entity.ShipmentStatus;
import com.dianora.backend.shipment.repository.ShipmentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ShipmentResponse createShipment(
            Long orderId,
            CreateShipmentRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));

        if ("CANCELLED".equals(order.getStatus().name())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create shipment for a cancelled order"
            );
        }

        List<ShipmentStatus> activeStatuses = List.of(
                ShipmentStatus.PENDING,
                ShipmentStatus.READY_TO_SHIP,
                ShipmentStatus.SHIPPED,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY
        );

        if (shipmentRepository.existsByOrder_IdAndStatusIn(
                orderId, activeStatuses)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active shipment already exists for this order"
            );
        }

        validateTrackingNumber(request.trackingNumber());

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCourierName(request.courierName());
        shipment.setTrackingNumber(request.trackingNumber());
        shipment.setTrackingUrl(request.trackingUrl());
        shipment.setEstimatedDeliveryAt(
                request.estimatedDeliveryAt()
        );
        shipment.setNotes(request.notes());
        shipment.setStatus(ShipmentStatus.PENDING);

        Shipment saved = shipmentRepository.save(shipment);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shipment not found"
                ));

        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrder(
            Long orderId) {

        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found"
            );
        }

        return shipmentRepository
                .findAllByOrder_IdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponse updateShipment(
            Long shipmentId,
            UpdateShipmentRequest request) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shipment not found"
                ));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.RETURNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A delivered or returned shipment cannot be updated"
            );
        }

        if (request.courierName() != null) {
            shipment.setCourierName(request.courierName());
        }

        if (request.trackingNumber() != null) {
            validateTrackingNumber(request.trackingNumber());

            if (!request.trackingNumber().equals(
                    shipment.getTrackingNumber())
                    && shipmentRepository.existsByTrackingNumber(
                    request.trackingNumber())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Tracking number already exists"
                );
            }

            shipment.setTrackingNumber(request.trackingNumber());
        }

        if (request.trackingUrl() != null) {
            shipment.setTrackingUrl(request.trackingUrl());
        }

        if (request.estimatedDeliveryAt() != null) {
            shipment.setEstimatedDeliveryAt(
                    request.estimatedDeliveryAt()
            );
        }

        if (request.notes() != null) {
            shipment.setNotes(request.notes());
        }

        if (request.status() != null) {
            updateStatus(shipment, request.status());
        }

        Shipment saved = shipmentRepository.save(shipment);
        return toResponse(saved);
    }

    private void updateStatus(
            Shipment shipment,
            ShipmentStatus newStatus) {

        ShipmentStatus current = shipment.getStatus();

        if (current == newStatus) {
            return;
        }

        if (current == ShipmentStatus.DELIVERED
                || current == ShipmentStatus.RETURNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Shipment is already in a final status"
            );
        }

        if (newStatus == ShipmentStatus.SHIPPED
                && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
        }

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        shipment.setStatus(newStatus);
    }

    private void validateTrackingNumber(String trackingNumber) {
        if (trackingNumber != null
                && trackingNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tracking number cannot be blank"
            );
        }
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrder().getId(),
                shipment.getOrder().getOrderNumber(),
                shipment.getCourierName(),
                shipment.getTrackingNumber(),
                shipment.getTrackingUrl(),
                shipment.getStatus(),
                shipment.getShippedAt(),
                shipment.getEstimatedDeliveryAt(),
                shipment.getDeliveredAt(),
                shipment.getNotes(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}