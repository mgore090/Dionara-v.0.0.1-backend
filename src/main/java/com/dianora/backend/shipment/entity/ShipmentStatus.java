package com.dianora.backend.shipment.entity;

public enum ShipmentStatus {
    PENDING,
    READY_TO_SHIP,
    SHIPPED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    RETURNED
}