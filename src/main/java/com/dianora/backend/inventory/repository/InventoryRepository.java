package com.dianora.backend.inventory.repository;

import com.dianora.backend.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<StockMovement, Long> {
}
