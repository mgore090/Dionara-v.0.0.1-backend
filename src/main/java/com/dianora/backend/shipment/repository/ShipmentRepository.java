package com.dianora.backend.shipment.repository;

import com.dianora.backend.shipment.entity.Shipment;
import com.dianora.backend.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long> {

    List<Shipment> findAllByOrder_IdOrderByCreatedAtDesc(Long orderId);

    boolean existsByOrder_IdAndStatusIn(
            Long orderId,
            List<ShipmentStatus> statuses
    );

    boolean existsByTrackingNumber(String trackingNumber);
}