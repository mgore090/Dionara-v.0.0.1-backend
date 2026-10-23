package com.dianora.backend.shipment.controller;

import com.dianora.backend.shipment.dto.CreateShipmentRequest;
import com.dianora.backend.shipment.dto.ShipmentResponse;
import com.dianora.backend.shipment.dto.UpdateShipmentRequest;
import com.dianora.backend.shipment.service.ShipmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/orders/{orderId}/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateShipmentRequest request) {
        return shipmentService.createShipment(orderId, request);
    }

    @GetMapping("/shipments/{shipmentId}")
    public ShipmentResponse getShipment(
            @PathVariable Long shipmentId) {
        return shipmentService.getShipment(shipmentId);
    }

    @GetMapping("/orders/{orderId}/shipments")
    public List<ShipmentResponse> getShipmentsByOrder(
            @PathVariable Long orderId) {
        return shipmentService.getShipmentsByOrder(orderId);
    }

    @PatchMapping("/shipments/{shipmentId}")
    public ShipmentResponse updateShipment(
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return shipmentService.updateShipment(shipmentId, request);
    }
}