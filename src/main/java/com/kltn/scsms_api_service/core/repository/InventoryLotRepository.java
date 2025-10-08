package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, UUID> {
    List<InventoryLot> findByWarehouseIdAndProductProductIdOrderByReceivedAtAsc(UUID warehouseId, UUID productId); // FIFO
}
