package com.dianora.backend.shipment.dto;

import com.dianora.backend.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateShipmentRequest(

        ShipmentStatus status,

        @Size(max = 100)
        String courierName,

        @Size(max = 150)
        String trackingNumber,

        @Size(max = 500)
        String trackingUrl,

        LocalDateTime estimatedDeliveryAt,

        @Size(max = 1000)
        String notes

) {}