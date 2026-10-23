package com.dianora.backend.shipment.entity;

import com.dianora.backend.order.entity.Order;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipment_order_id", columnList = "order_id"),
                @Index(name = "idx_shipment_tracking_number",
                        columnList = "tracking_number")
        }
)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "courier_name", length = 100)
    private String courierName;

    @Column(name = "tracking_number", unique = true, length = 150)
    private String trackingNumber;

    @Column(length = 500)
    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    private LocalDateTime shippedAt;

    private LocalDateTime estimatedDeliveryAt;

    private LocalDateTime deliveredAt;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = ShipmentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getEstimatedDeliveryAt() {
        return estimatedDeliveryAt;
    }

    public void setEstimatedDeliveryAt(
            LocalDateTime estimatedDeliveryAt) {
        this.estimatedDeliveryAt = estimatedDeliveryAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}