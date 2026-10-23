package com.dianora.backend.shipment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateShipmentRequest(

        @Size(max = 100)
        String courierName,

        @Size(max = 150)
        String trackingNumber,

        @Size(max = 500)
        String trackingUrl,

        @FutureOrPresent
        LocalDateTime estimatedDeliveryAt,

        @Size(max = 1000)
        String notes

) {}