package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, UUID> {
    List<InventoryLot> findByBranch_BranchIdAndProduct_ProductIdOrderByReceivedAtAsc(UUID branchId, UUID productId); // FIFO

    List<InventoryLot> findAllByBranch_BranchId(UUID branchBranchId);

    List<InventoryLot> findAllByProduct_ProductId(UUID productId);

    List<InventoryLot> findAllByProduct_ProductIdAndBranch_BranchId(UUID productId, UUID branchId);
}
