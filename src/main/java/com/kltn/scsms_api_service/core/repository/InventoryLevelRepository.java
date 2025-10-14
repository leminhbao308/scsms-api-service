package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, UUID> {
    Optional<InventoryLevel> findByWarehouseIdAndProductProductId(UUID warehouseId, UUID productId);
    
    List<InventoryLevel> findAllByWarehouse_Id(UUID warehouseId);
}
