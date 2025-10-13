package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByBranch_BranchId(UUID branchBranchId);
    
    List<PurchaseOrder> findByCreatedDateLessThanEqualAndBranch_BranchId(LocalDateTime createdDateIsLessThan, UUID branchBranchId);
    
    List<PurchaseOrder> findByCreatedDateGreaterThanEqualAndBranch_BranchId(LocalDateTime createdDateIsGreaterThan, UUID branchBranchId);
    
    List<PurchaseOrder> findByCreatedDateBetween(LocalDateTime createdDateAfter, LocalDateTime createdDateBefore);
    
    List<PurchaseOrder> findByCreatedDateBetweenAndBranch_BranchId(LocalDateTime createdDateAfter, LocalDateTime createdDateBefore, UUID branchBranchId);
}
