package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryHeader;
import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryHeaderRepository extends JpaRepository<InventoryHeader, UUID> {

    Optional<InventoryHeader> findByInventoryCode(String inventoryCode);
    
    @Query("SELECT i FROM InventoryHeader i WHERE " +
           "(:inventoryCode IS NULL OR i.inventoryCode LIKE %:inventoryCode%) AND " +
           "(:transactionType IS NULL OR i.transactionType = :transactionType) AND " +
           "(:branchId IS NULL OR i.branch.branchId = :branchId) AND " +
           "(:supplierId IS NULL OR i.supplier.supplierId = :supplierId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:fromDate IS NULL OR i.transactionDate >= :fromDate) AND " +
           "(:toDate IS NULL OR i.transactionDate <= :toDate)")
    Page<InventoryHeader> findInventoryHeadersByFilters(
            @Param("inventoryCode") String inventoryCode,
            @Param("transactionType") TransactionType transactionType,
            @Param("branchId") UUID branchId,
            @Param("supplierId") UUID supplierId,
            @Param("status") InventoryStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    @Query("SELECT i FROM InventoryHeader i JOIN i.branch b " +
           "WHERE b.branchId = :branchId AND i.transactionType = :transactionType")
    List<InventoryHeader> findByBranchAndTransactionType(
            @Param("branchId") UUID branchId,
            @Param("transactionType") TransactionType transactionType);
}
