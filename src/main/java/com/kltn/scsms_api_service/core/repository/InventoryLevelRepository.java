package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, UUID> {
    List<InventoryLevel> findAllByBranch_BranchId(UUID branchId);
    
    Optional<InventoryLevel> findByBranch_BranchIdAndProduct_ProductId(UUID branchBranchId, UUID productProductId);
}
